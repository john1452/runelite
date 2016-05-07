package net.runelite.asm.attributes.code.instructions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.runelite.asm.Field;
import net.runelite.asm.attributes.code.Instruction;
import net.runelite.asm.attributes.code.InstructionType;
import net.runelite.asm.attributes.code.Instructions;
import net.runelite.asm.attributes.code.instruction.types.ComparisonInstruction;
import net.runelite.asm.attributes.code.instruction.types.GetFieldInstruction;
import net.runelite.asm.attributes.code.instruction.types.JumpingInstruction;
import net.runelite.asm.attributes.code.instruction.types.MappableInstruction;
import net.runelite.asm.attributes.code.instruction.types.PushConstantInstruction;
import net.runelite.asm.execution.Frame;
import net.runelite.asm.execution.InstructionContext;
import net.runelite.asm.execution.Stack;
import net.runelite.asm.execution.StackContext;
import net.runelite.deob.deobfuscators.mapping.MappingExecutorUtil;
import net.runelite.deob.deobfuscators.mapping.PacketHandler;
import net.runelite.deob.deobfuscators.mapping.ParallelExecutorMapping;

public abstract class If extends Instruction implements JumpingInstruction, ComparisonInstruction, MappableInstruction
{
	private Instruction to;
	private short offset;

	public If(Instructions instructions, InstructionType type, int pc)
	{
		super(instructions, type, pc);
	}
	
	public If(Instructions instructions, InstructionType type, Instruction to)
	{
		super(instructions, type, -1);
		
		this.to = to;
	}
	
	public If(Instructions instructions, Instruction to)
	{
		super(instructions, InstructionType.IF_ICMPNE, -1);
		
		assert this != to;
		assert to.getInstructions() == this.getInstructions();
		
		this.to = to;
	}
	
	@Override
	public void load(DataInputStream is) throws IOException
	{
		offset = is.readShort();
		length += 2;
	}
	
	@Override
	public void resolve()
	{
		to = this.getInstructions().findInstruction(this.getPc() + offset);
	}
	
	@Override
	public void write(DataOutputStream out) throws IOException
	{
		super.write(out);
		out.writeShort(to.getPc() - this.getPc());
	}
	
	@Override
	public InstructionContext execute(Frame frame)
	{
		InstructionContext ins = new InstructionContext(this, frame);
		Stack stack = frame.getStack();
		
		StackContext one = stack.pop();
		StackContext two = stack.pop();
		
		ins.pop(one, two);
		
		Frame other = frame.dup();
		other.jump(ins, to);

		ins.branch(other);
		
		return ins;
	}
	
	@Override
	public void replace(Instruction oldi, Instruction newi)
	{
		if (to == oldi)
			to = newi;
	}
	
	@Override
	public List<Instruction> getJumps()
	{
		return Arrays.asList(to);
	}
	
	@Override
	public void map(ParallelExecutorMapping mapping, InstructionContext ctx, InstructionContext other)
	{
		assert ctx.getBranches().size() == other.getBranches().size();
		
		// can be empty for packet handlers
		if (!ctx.getBranches().isEmpty())
		{
			Frame branch1 = ctx.getBranches().get(0),
				branch2 = other.getBranches().get(0);

			assert branch1.other == null;
			assert branch2.other == null;

			branch1.other = branch2;
			branch2.other = branch1;
		}
		
		this.mapArguments(mapping, ctx, other, false);
	}
	
	protected void mapOtherBranch(ParallelExecutorMapping mapping, InstructionContext ctx, InstructionContext other)
	{
		Frame f1 = ctx.getFrame(),
			f2 = other.getFrame(),
			branch1 = ctx.getBranches().get(0),
			branch2 = other.getBranches().get(0);

		assert branch1.other == null;
		assert branch2.other == null;

		// currently f1 <-> f2
		assert f1.other == f2;
		assert f2.other == f1;

		// change to f1 <-> branch2, f2 <-> branch1

		f1.other = branch2;
		branch2.other = f1;

		f2.other = branch1;
		branch1.other = f2;

		this.mapArguments(mapping, ctx, other, true);
	}
	
	private void mapArguments(ParallelExecutorMapping mapping, InstructionContext ctx, InstructionContext other, boolean inverse)
	{
		List<Field> f1s = getComparedFields(ctx), f2s = getComparedFields(other);
		
		if (f1s == null || f2s == null || f1s.size() != f2s.size())
			return;

		if (f1s.size() == 1)
		{
			Field f1 = f1s.get(0), f2 = f2s.get(0);

			assert MappingExecutorUtil.isMaybeEqual(f1.getType(), f2.getType());

			mapping.map(f1, f2);

			if (f1.packetHandler && f2.packetHandler)
			{
				int pc1 = this.getConstantInstruction(ctx),
					pc2 = this.getConstantInstruction(other);

				assert (pc1 != -1) == (pc2 != -1);

				if (pc1 == -1 && pc2 == -1)
					return;

				mapping.packetHandler1.add(new PacketHandler(this, pc1));
				mapping.packetHandler2.add(new PacketHandler((If) other.getInstruction(), pc2));
			}
		}
		else if (f1s.size() == 2)
		{
			Field f1 = f1s.get(0), f2 = f2s.get(0);
			Field j1 = f1s.get(1), j2 = f2s.get(1);

			
//			if (couldBeSame(f1, f2) && couldBeSame(j1, j2) && couldBeSame(f1, j2) && couldBeSame(j1, f2))
//			{
//				mapping.map()
//				return; // ambiguous
//			}
			
			if (couldBeSame(f1, f2) && couldBeSame(j1, j2))
			{
				mapping.map(f1, f2);
				mapping.map(j1, j2);
			}
			
			if (couldBeSame(f1, j2) && couldBeSame(j1, f2))
			{
				mapping.map(f1, j2);
				mapping.map(j1, f2);
			}
		}
		else
			assert false;
	}
	
	private List<Field> getComparedFields(InstructionContext ctx)
	{
		List<Field> fields = new ArrayList<>();
		
		for (StackContext sctx : ctx.getPops())
		{
			InstructionContext base = MappingExecutorUtil.resolve(sctx.getPushed(), sctx);
			
			if (base.getInstruction() instanceof GetFieldInstruction)
			{
				GetFieldInstruction gfi = (GetFieldInstruction) base.getInstruction();
				
				if (gfi.getMyField() != null)
					fields.add(gfi.getMyField());
			}
		}

		return fields.isEmpty() ? null : fields;
	}

	private Integer getConstantInstruction(InstructionContext ctx)
	{
		PushConstantInstruction gfi = null;

		for (StackContext sctx : ctx.getPops())
		{
			InstructionContext base = MappingExecutorUtil.resolve(sctx.getPushed(), sctx);

			if (base.getInstruction() instanceof PushConstantInstruction)
			{
				if (gfi != null)
					return null;

				gfi = (PushConstantInstruction) base.getInstruction();
			}
		}

		return (Integer) gfi.getConstant().getObject();
	}
	
	private boolean couldBeSame(Field f1, Field f2)
	{
		if (f1.isStatic() != f2.isStatic())
			return false;

		if (!f1.isStatic())
			if (!MappingExecutorUtil.isMaybeEqual(f1.getFields().getClassFile(), f2.getFields().getClassFile()))
				return false;

		return MappingExecutorUtil.isMaybeEqual(f1.getType(), f2.getType());
	}
	
	protected boolean isSameField(InstructionContext thisIc, InstructionContext otherIc)
	{
		List<Field> f1s = getComparedFields(thisIc), f2s = getComparedFields(otherIc);

		if ((f1s != null) != (f2s != null))
			return false;
		
		if (f1s == null || f2s == null)
			return true;

		if (f1s.size() != f2s.size())
			return false;

		assert f1s.size() == 1 || f1s.size() == 2;

		if (f1s.size() == 2)
		{
			Field f1 = f1s.get(0), f2 = f2s.get(0);
			Field j1 = f1s.get(1), j2 = f2s.get(1);
			
			if (couldBeSame(f1, f2) && couldBeSame(j1, j2) && couldBeSame(f1, j2) && couldBeSame(j1, f2))
				return true;
			
			if (couldBeSame(f1, f2) && couldBeSame(j1, j2))
				return true;
			
			if (couldBeSame(f1, j2) && couldBeSame(j1, f2))
				return true;
			
			return false;
		}
		else
		{
			Field f1 = f1s.get(0), f2 = f2s.get(0);
			
			return couldBeSame(f1, f2);
		}
	}
	
	@Override
	public boolean canMap(InstructionContext thisIc)
	{
		return true;
	}
}