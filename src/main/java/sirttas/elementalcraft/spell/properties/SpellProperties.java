package sirttas.elementalcraft.spell.properties;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import sirttas.dpanvil.api.codec.CodecHelper;
import sirttas.dpanvil.api.codec.Codecs;
import sirttas.dpanvil.api.event.DataManagerReloadEvent;
import sirttas.elementalcraft.api.ElementalCraftApi;
import sirttas.elementalcraft.api.element.ElementType;
import sirttas.elementalcraft.api.element.IElementTypeProvider;
import sirttas.elementalcraft.api.name.ECNames;
import sirttas.elementalcraft.gui.ECColorHelper;
import sirttas.elementalcraft.spell.Spell;

@Mod.EventBusSubscriber(modid = ElementalCraftApi.MODID)
public class SpellProperties implements IElementTypeProvider {

	public static final String NAME = "spell_properties";
	public static final String FOLDER = ElementalCraftApi.MODID + '_' + NAME;

	public static final SpellProperties NONE = new SpellProperties();
	public static final Codec<SpellProperties> CODEC = RecordCodecBuilder.create(builder -> builder.group(
			Spell.Type.CODEC.fieldOf(ECNames.SPELL_TYPE).forGetter(SpellProperties::getSpellType),
			ElementType.forGetter(SpellProperties::getElementType),
			Codec.INT.optionalFieldOf(ECNames.WEIGHT, 0).forGetter(SpellProperties::getWeight),
			Codec.INT.optionalFieldOf(ECNames.USE_DURATION, 0).forGetter(SpellProperties::getUseDuration),
			Codec.INT.optionalFieldOf(ECNames.ELEMENT_CONSUMPTION, 0).forGetter(SpellProperties::getConsumeAmount),
			Codec.INT.optionalFieldOf(ECNames.COOLDOWN, 0).forGetter(SpellProperties::getCooldown),
			Codec.FLOAT.optionalFieldOf(ECNames.RANGE, 0F).forGetter(SpellProperties::getRange),
			Codecs.COLOR.optionalFieldOf(ECNames.COLOR, -1).forGetter(SpellProperties::getColor),
			Codecs.ATTRIBUTE_MULTIMAP.optionalFieldOf(ECNames.ATTRIBUTES, Multimaps.forMap(Collections.emptyMap())).forGetter(SpellProperties::getAttributes)
	).apply(builder, SpellProperties::new));

	private final int cooldown;
	private final int consumeAmount;
	private final int useDuration;
	private final int weight;
	private final int color;
	private final float range;
	private final ElementType elementType;
	private final Spell.Type spellType;
	private final Multimap<Attribute, AttributeModifier> attributes;

	public SpellProperties() {
		this(Spell.Type.NONE, ElementType.NONE, 0, 0, 0, 0, 0, -1, null);
	}

	private SpellProperties(Spell.Type spellType, ElementType elementType, int weight, int useDuration, int consumeAmount, int cooldown, float range, int color, Multimap<Attribute, AttributeModifier> attributes) {
		this.spellType = spellType;
		this.elementType = elementType;
		this.weight = weight;
		this.useDuration = useDuration;
		this.consumeAmount = consumeAmount;
		this.cooldown = cooldown;
		this.range = range;
		this.color = color;
		this.attributes = attributes != null ? Multimaps.unmodifiableMultimap(attributes) : Multimaps.forMap(Collections.emptyMap());
	}

	public int getCooldown() {
		return cooldown;
	}

	public int getConsumeAmount() {
		return consumeAmount;
	}

	public int getUseDuration() {
		return useDuration;
	}

	public int getWeight() {
		return weight;
	}

	@Override
	public ElementType getElementType() {
		return elementType;
	}

	public Spell.Type getSpellType() {
		return spellType;
	}

	public float getRange() {
		return range;
	}

	public int getColor() {
		return color;
	}
	

	public Multimap<Attribute, AttributeModifier> getAttributes() {
		return attributes;
	}
	
	@SubscribeEvent
	public static void onReload(DataManagerReloadEvent<SpellProperties> event) {
		Map<ResourceLocation, SpellProperties> data = event.getDataManager().getData();

		for (Spell spell : Spell.REGISTRY.getValues()) {
			SpellProperties prop = data.get(spell.getRegistryName());

			if (prop != null) {
				spell.setProperties(prop);
			} else {
				spell.setProperties(SpellProperties.NONE);
			}
		}
	}

	public static final class Builder {

		public static final Encoder<Builder> ENCODER = CodecHelper.remapField(SpellProperties.CODEC, Codecs.HEX_COLOR.fieldOf(ECNames.COLOR), p -> p.color)
				.comap(builder -> new SpellProperties(builder.type, builder.elementType, builder.weight, builder.useDuration, builder.consumeAmount, builder.cooldown, (float) builder.range, builder.color, builder.attributes));

		private int cooldown;
		private int consumeAmount;
		private int useDuration;
		private int weight;
		private int color;
		private double range;
		private ElementType elementType;
		private final Spell.Type type;
		private final Multimap<Attribute, AttributeModifier> attributes;

		private Builder(Spell.Type type) {
			this.type = type;
			this.elementType = ElementType.NONE;
			cooldown = 0;
			consumeAmount = 0;
			useDuration = 0;
			weight = 0;
			attributes = HashMultimap.create();
		}

		public static Builder create(Spell.Type type) {
			return new Builder(type);
		}

		public Builder cooldown(int cooldown) {
			this.cooldown = cooldown;
			return this;
		}

		public Builder consumeAmount(int consumeAmount) {
			this.consumeAmount = consumeAmount;
			return this;
		}

		public Builder useDuration(int useDuration) {
			this.useDuration = useDuration;
			return this;
		}


		public Builder elementType(ElementType elementType) {
			this.elementType = elementType;
			return this;
		}

		public Builder weight(int weight) {
			this.weight = weight;
			return this;
		}

		public Builder range(double range) {
			this.range = range;
			return this;
		}

		public Builder color(int color) {
			this.color = color;
			return this;
		}

		public Builder addAttribute(Attribute attribute, AttributeModifier modifier) {
			this.attributes.put(attribute, modifier);
			return this;
		}
		
		public Builder color(int r, int g, int b) {
			return color(ECColorHelper.packColor(r, g, b));
		}

		public JsonElement toJson() {
			return CodecHelper.encode(ENCODER, this);
		}
	}
}
