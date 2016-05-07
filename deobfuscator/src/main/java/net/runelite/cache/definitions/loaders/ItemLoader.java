package net.runelite.cache.definitions.loaders;

import java.util.ArrayList;
import java.util.List;
import net.runelite.cache.IndexType;
import net.runelite.cache.definitions.ItemDefinition;
import net.runelite.cache.io.InputStream;
import net.runelite.cache.utils.StringUtilities;

public class ItemLoader
{
	public static final IndexType INDEX_TYPE = IndexType.TWO;
	public static final int ARCHIVE_ID = 10;

	private final List<ItemDefinition> items = new ArrayList<>();

	public List<ItemDefinition> getItems()
	{
		return items;
	}

	public void load(int id, InputStream stream)
	{
		ItemDefinition def = new ItemDefinition(id);
		while (true)
		{
			int opcode = stream.readUnsignedByte();
			if (opcode == 0)
			{
				break;
			}

			this.decodeValues(opcode, def, stream);
		}
		items.add(def);
	}

	private void decodeValues(int opcode, ItemDefinition def, InputStream stream)
	{
		if (opcode == 1)
		{
			def.inventoryModel = stream.readUnsignedShort();
		}
		else if (opcode == 2)
		{
			def.name = StringUtilities.readString_2(stream);
		}
		else if (opcode == 4)
		{
			def.zoom2d = stream.readUnsignedShort();
		}
		else if (opcode == 5)
		{
			def.xan2d = stream.readUnsignedShort();
		}
		else if (opcode == 6)
		{
			def.yan2d = stream.readUnsignedShort();
		}
		else if (7 == opcode)
		{
			def.xOffset2d = stream.readUnsignedShort();
			if (def.xOffset2d > 32767)
			{
				def.xOffset2d -= 65536;
			}
		}
		else if (8 == opcode)
		{
			def.yOffset2d = stream.readUnsignedShort();
			if (def.yOffset2d > 32767)
			{
				def.yOffset2d -= 65536;
			}
		}
		else if (11 == opcode)
		{
			def.stackable = 1;
		}
		else if (opcode == 12)
		{
			def.cost = stream.readInt();
		}
		else if (16 == opcode)
		{
			def.members = true;
		}
		else if (opcode == 23)
		{
			def.maleModel0 = stream.readUnsignedShort();
			def.maleOffset = stream.readUnsignedByte();
		}
		else if (opcode == 24)
		{
			def.maleModel1 = stream.readUnsignedShort();
		}
		else if (25 == opcode)
		{
			def.femaleModel0 = stream.readUnsignedShort();
			def.femaleOffset = stream.readUnsignedByte();
		}
		else if (26 == opcode)
		{
			def.femaleModel1 = stream.readUnsignedShort();
		}
		else if (opcode >= 30 && opcode < 35)
		{
			def.options[opcode - 30] = StringUtilities.readString_2(stream);
			if (def.options[opcode - 30].equalsIgnoreCase("Hidden"))
			{
				def.options[opcode - 30] = null;
			}
		}
		else if (opcode >= 35 && opcode < 40)
		{
			def.interfaceOptions[opcode - 35] = StringUtilities.readString_2(stream);
		}
		else
		{
			int var4;
			int var5;
			if (opcode == 40)
			{
				var5 = stream.readUnsignedByte();
				def.colorFind = new short[var5];
				def.colorReplace = new short[var5];

				for (var4 = 0; var4 < var5; ++var4)
				{
					def.colorFind[var4] = (short) stream.readUnsignedShort();
					def.colorReplace[var4] = (short) stream.readUnsignedShort();
				}

			}
			else if (41 != opcode)
			{
				if (opcode == 78)
				{
					def.maleModel2 = stream.readUnsignedShort();
				}
				else if (opcode == 79)
				{
					def.femaleModel2 = stream.readUnsignedShort();
				}
				else if (90 == opcode)
				{
					def.maleHeadModel = stream.readUnsignedShort();
				}
				else if (91 == opcode)
				{
					def.femaleHeadModel = stream.readUnsignedShort();
				}
				else if (92 == opcode)
				{
					def.maleHeadModel2 = stream.readUnsignedShort();
				}
				else if (opcode == 93)
				{
					def.femaleHeadModel2 = stream.readUnsignedShort();
				}
				else if (opcode == 95)
				{
					def.zan2d = stream.readUnsignedShort();
				}
				else if (97 == opcode)
				{
					def.notedID = stream.readUnsignedShort();
				}
				else if (98 == opcode)
				{
					def.notedTemplate = stream.readUnsignedShort();
				}
				else if (opcode >= 100 && opcode < 110)
				{
					if (def.countObj == null)
					{
						def.countObj = new int[10];
						def.countCo = new int[10];
					}

					def.countObj[opcode - 100] = stream.readUnsignedShort();
					def.countCo[opcode - 100] = stream.readUnsignedShort();
				}
				else if (110 == opcode)
				{
					def.resizeX = stream.readUnsignedShort();
				}
				else if (opcode == 111)
				{
					def.resizeY = stream.readUnsignedShort();
				}
				else if (opcode == 112)
				{
					def.resizeZ = stream.readUnsignedShort();
				}
				else if (opcode == 113)
				{
					def.ambient = stream.readByte();
				}
				else if (114 == opcode)
				{
					def.contrast = stream.readByte();
				}
				else if (115 == opcode)
				{
					def.team = stream.readUnsignedByte();
				}
			}
			else
			{
				var5 = stream.readUnsignedByte();
				def.textureFind = new short[var5];
				def.textureReplace = new short[var5];

				for (var4 = 0; var4 < var5; ++var4)
				{
					def.textureFind[var4] = (short) stream.readUnsignedShort();
					def.textureReplace[var4] = (short) stream.readUnsignedShort();
				}

			}
		}
	}
}