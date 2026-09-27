package nadiendev.kubejsastralsorcery.constellation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.client.LangKubeEvent;
import dev.latvian.mods.kubejs.plugin.builtin.wrapper.StringUtilsWrapper;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.rhino.util.HideFromJS;
import hellfirepvp.astralsorcery.common.constellation.BaseConstellation;
import hellfirepvp.astralsorcery.common.constellation.MoonPhase;
import hellfirepvp.astralsorcery.common.constellation.property.AttunePlayerProperty;
import hellfirepvp.astralsorcery.common.constellation.property.FocusCrystalProperty;
import hellfirepvp.astralsorcery.common.constellation.property.FocusCrystalSortProperty;
import hellfirepvp.astralsorcery.common.constellation.property.ShowUpConditionProperty;
import hellfirepvp.astralsorcery.common.constellation.star.StarLocation;
import hellfirepvp.astralsorcery.common.focal.FocusCrystalFunction;
import hellfirepvp.astralsorcery.common.focal.FocusCrystalPlacementHelper;
import hellfirepvp.astralsorcery.common.focal.FocusCrystalVisualSortFunction;
import hellfirepvp.astralsorcery.common.focal.FocusCrystalVisualSortHelper;
import hellfirepvp.astralsorcery.common.util.data.ColorWrapper;
import nadiendev.kubejsastralsorcery.recipe.component.ColorComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ConstellationBuilder extends BuilderBase<BaseConstellation> {
    public static final Map<String, FocusCrystalFunction> FOCUS_LAYOUTS = Map.of(
        "opposite_no_axis_symmetry", FocusCrystalPlacementHelper::hasOppositeButNoAxisSymmetry,
        "one_axis_symmetry", FocusCrystalPlacementHelper::hasOneAxisSymmetryButNotOther,
        "unique_distances", FocusCrystalPlacementHelper::allHaveUniqueDistances,
        "no_right_angles", FocusCrystalPlacementHelper::noneHaveRightAngles,
        "no_dot_no_axis_symmetry", FocusCrystalPlacementHelper::noDotNoAxisSymmetry,
        "any", FocusCrystalPlacementHelper::allPositionsFulfillGlobalRules
    );

    public static final Map<String, FocusCrystalVisualSortFunction> FOCUS_SORTS = Map.of(
        "polygon", FocusCrystalVisualSortHelper::sortIntoContinuousPolygon,
        "closest_first", FocusCrystalVisualSortHelper::sortByClosestFirst
    );

    public static final List<String> ROOT_PERK_TYPES = List.of("aevitas", "armara", "discidia", "evorsio", "vicio");

    public static final Map<String, String> ROOT_PERK_DESCRIPTIONS = Map.of(
        "aevitas", "Gain experience by placing blocks",
        "armara", "Gain experience by taking damage",
        "discidia", "Gain experience by dealing damage",
        "evorsio", "Gain experience by breaking blocks",
        "vicio", "Gain experience by moving"
    );

    public transient int color = 0xFFFFFFFF;
    public transient String tier = "major";
    public transient final List<int[]> stars = new ArrayList<>();
    public transient final List<int[]> connections = new ArrayList<>();
    public transient String focusLayout;
    public transient String focusSort = "polygon";
    public transient boolean focalPoint;
    public transient boolean attunable;
    public transient List<MoonPhase> phases = List.of();
    public transient boolean seededPhases;
    public transient float phaseDropOff = 0.25F;
    public transient String subtitle;
    public transient String description;
    public transient String rootPerkType;
    public transient float rootPerkX;
    public transient float rootPerkY;
    public transient final List<String> rootPerkConnections = new ArrayList<>();
    public transient final List<JsonObject> rootPerkModifiers = new ArrayList<>();
    public transient final List<float[]> skyPositions = new ArrayList<>();

    public ConstellationBuilder(ResourceLocation id) {
        super(id);
    }

    @Override
    public String getTranslationKeyGroup() {
        return "constellation";
    }

    public ConstellationBuilder color(Object color) {
        this.color = ColorComponent.parse(color);
        return this;
    }

    public ConstellationBuilder major() {
        return tier("major");
    }

    public ConstellationBuilder minor() {
        return tier("minor");
    }

    public ConstellationBuilder hidden() {
        return tier("hidden");
    }

    public ConstellationBuilder tier(String tier) {
        String t = tier.toLowerCase(Locale.ROOT);

        if (!t.equals("major") && !t.equals("minor") && !t.equals("hidden")) {
            throw new IllegalArgumentException("Constellation tier must be major, minor or hidden, got '" + tier + "'");
        }

        this.tier = t;
        return this;
    }

    public ConstellationBuilder star(int x, int y) {
        if (x < 0 || x >= BaseConstellation.STAR_GRID_WIDTH_HEIGHT || y < 0 || y >= BaseConstellation.STAR_GRID_WIDTH_HEIGHT) {
            throw new IllegalArgumentException("Star " + x + "," + y + " is outside the 0-31 grid");
        }

        for (int[] s : stars) {
            if (s[0] == x && s[1] == y) {
                throw new IllegalArgumentException("Star " + x + "," + y + " is defined twice");
            }
        }

        stars.add(new int[]{x, y});
        return this;
    }

    public ConstellationBuilder stars(int[][] positions) {
        for (int[] p : positions) {
            star(p[0], p[1]);
        }

        return this;
    }

    public ConstellationBuilder connect(int from, int to) {
        connections.add(new int[]{from, to});
        return this;
    }

    public ConstellationBuilder connections(int[][] pairs) {
        for (int[] p : pairs) {
            connect(p[0], p[1]);
        }

        return this;
    }

    public ConstellationBuilder chain(int... indices) {
        for (int i = 1; i < indices.length; i++) {
            connect(indices[i - 1], indices[i]);
        }

        return this;
    }

    public ConstellationBuilder focusCrystal(String layout) {
        String l = layout.toLowerCase(Locale.ROOT);

        if (!FOCUS_LAYOUTS.containsKey(l)) {
            throw new IllegalArgumentException("Unknown focus crystal layout '" + layout + "'. Valid: " + FOCUS_LAYOUTS.keySet());
        }

        this.focusLayout = l;
        return this;
    }

    public ConstellationBuilder focusSort(String sort) {
        String s = sort.toLowerCase(Locale.ROOT);

        if (!FOCUS_SORTS.containsKey(s)) {
            throw new IllegalArgumentException("Unknown focus sort '" + sort + "'. Valid: " + FOCUS_SORTS.keySet());
        }

        this.focusSort = s;
        return this;
    }

    public ConstellationBuilder focalPoint() {
        this.focalPoint = true;

        if (focusLayout == null) {
            focusLayout = "any";
        }

        return this;
    }

    public ConstellationBuilder attunable() {
        this.attunable = true;
        return this;
    }

    public ConstellationBuilder moonPhases(String... phases) {
        return phases(false, phases);
    }

    public ConstellationBuilder seededMoonPhases(String... phases) {
        return phases(true, phases);
    }

    public ConstellationBuilder phaseDropOff(float dropOff) {
        this.phaseDropOff = dropOff;
        return this;
    }

    private ConstellationBuilder phases(boolean seeded, String... names) {
        List<MoonPhase> list = new ArrayList<>();

        for (String name : names) {
            list.add(MoonPhase.valueOf(name.toUpperCase(Locale.ROOT)));
        }

        this.phases = list;
        this.seededPhases = seeded;
        return this;
    }

    public ConstellationBuilder subtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    public ConstellationBuilder description(String description) {
        this.description = description;
        return this;
    }

    public ConstellationBuilder rootPerk(String type, float x, float y) {
        String t = type.toLowerCase(Locale.ROOT);

        if (!ROOT_PERK_TYPES.contains(t)) {
            throw new IllegalArgumentException("Root perk type must be one of " + ROOT_PERK_TYPES + ", got '" + type + "'");
        }

        this.rootPerkType = t;
        this.rootPerkX = x;
        this.rootPerkY = y;
        this.attunable = true;
        return this;
    }

    public ConstellationBuilder rootPerkConnect(String... perks) {
        for (String perk : perks) {
            rootPerkConnections.add(perk.indexOf(':') >= 0 ? perk : "astralsorcery:" + perk);
        }

        return this;
    }

    public ConstellationBuilder rootPerkModifier(String attribute, int mode, float value) {
        JsonObject modifier = new JsonObject();
        modifier.addProperty("attribute_type", attribute.indexOf(':') >= 0 ? attribute : "astralsorcery:" + attribute);
        modifier.addProperty("identifier", "perk_modifier_" + rootPerkModifiers.size());
        modifier.addProperty("mode", mode);
        modifier.addProperty("value", value);
        rootPerkModifiers.add(modifier);
        return this;
    }

    public ConstellationBuilder skyPosition(float yaw, float pitch) {
        if (pitch < 10 || pitch > 80 || yaw < 0 || yaw >= 360) {
            throw new IllegalArgumentException("Sky positions need yaw 0-359 and pitch 10-80");
        }

        skyPositions.add(new float[]{yaw, pitch});
        return this;
    }

    @Override
    @HideFromJS
    public BaseConstellation createObject() {
        if (stars.isEmpty()) {
            throw new IllegalStateException("Constellation " + id + " has no stars");
        }

        BaseConstellation.Builder<BaseConstellation> builder = BaseConstellation.builder(ColorWrapper.opaque(color));
        builder.tier(switch (tier) {
            case "minor" -> BaseConstellation.Tier.MINOR;
            case "hidden" -> BaseConstellation.Tier.HIDDEN;
            default -> BaseConstellation.Tier.MAJOR;
        });
        builder.sorted();

        if (focusLayout != null) {
            builder.withProperty(FocusCrystalProperty.KEY, FocusCrystalProperty.of(FOCUS_LAYOUTS.get(focusLayout)));
            builder.withProperty(FocusCrystalSortProperty.KEY, FocusCrystalSortProperty.of(FOCUS_SORTS.get(focusSort)));
        }

        if (attunable) {
            builder.withProperty(AttunePlayerProperty.KEY, AttunePlayerProperty.defaultRoot());
        }

        if (!phases.isEmpty()) {
            MoonPhase[] array = phases.toArray(MoonPhase[]::new);
            builder.withProperty(ShowUpConditionProperty.KEY, seededPhases
                ? ShowUpConditionProperty.forSeededMoonPhases(phaseDropOff, array)
                : ShowUpConditionProperty.forFixedMoonPhases(phaseDropOff, array));
        }

        List<StarLocation> locations = new ArrayList<>();

        for (int[] s : stars) {
            locations.add(builder.addStar(s[0], s[1]));
        }

        for (int[] c : connections) {
            if (c[0] < 0 || c[0] >= locations.size() || c[1] < 0 || c[1] >= locations.size()) {
                throw new IllegalArgumentException("Constellation " + id + " connects star " + c[0] + " to " + c[1] + " but only has " + locations.size() + " stars");
            }

            builder.addConnection(locations.get(c[0]), locations.get(c[1]));
        }

        return builder.build(BaseConstellation::new).get();
    }

    @HideFromJS
    public JsonObject createRootPerk() {
        JsonObject perk = new JsonObject();
        perk.addProperty("type", "astralsorcery:root_perk_" + rootPerkType);
        JsonObject category = new JsonObject();
        category.addProperty("color", -1);
        JsonObject name = new JsonObject();
        name.addProperty("translate", "perk.category.astralsorcery.root");
        category.add("name", name);
        perk.add("category", category);
        perk.addProperty("constellation", id.toString());
        perk.add("converters", new JsonArray());
        JsonArray modifiers = new JsonArray();
        rootPerkModifiers.forEach(modifiers::add);
        perk.add("modifiers", modifiers);
        perk.addProperty("name", "perk." + id.getNamespace() + ".root_" + id.getPath());
        perk.addProperty("registry_name", id.getNamespace() + ":root_" + id.getPath());
        JsonArray requirements = new JsonArray();
        JsonObject requirement = new JsonObject();
        requirement.addProperty("type", "astralsorcery:constellation");
        requirement.addProperty("constellation", id.toString());
        requirements.add(requirement);
        perk.add("requirements", requirements);
        perk.addProperty("x", rootPerkX);
        perk.addProperty("y", rootPerkY);
        JsonObject root = new JsonObject();
        JsonArray connections = new JsonArray();
        rootPerkConnections.forEach(connections::add);
        root.add("connections", connections);
        root.add("perk", perk);
        return root;
    }

    @Override
    @HideFromJS
    public void generateLang(LangKubeEvent lang) {
        super.generateLang(lang);
        String ns = id.getNamespace();
        String base = "tome.research.constellation." + ns + "." + id.getPath();

        if (subtitle != null) {
            lang.add(ns, base + ".subtitle", subtitle);
        }

        if (description != null) {
            lang.add(ns, base + ".description", description);
        }

        if (rootPerkType != null) {
            String perkName = "Root: " + (displayName != null ? displayName.getString() : StringUtilsWrapper.snakeCaseToTitleCase(id.getPath()));
            String perkKey = "perk." + ns + ".root_" + id.getPath();
            lang.add(ns, perkKey + ".name", perkName);
            lang.add(ns, perkKey + ".description", ROOT_PERK_DESCRIPTIONS.get(rootPerkType));
        }
    }
}
