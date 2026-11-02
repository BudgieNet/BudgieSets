package com.github.treemanking.budgiesets.hook;

import com.budgienet.budgieEnchants.config.EffectParser;
import com.budgienet.budgieEnchants.config.Problems;
import com.budgienet.budgieEnchants.core.Budgie;
import com.budgienet.budgieEnchants.engine.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Set bonuses: on only while the set is reported and all four pieces carry its tag; files parse strictly; old files copy over once. */
class SetBonusTest {
    static { Budgie.registerBuiltins(); }

    @TempDir
    Path root;

    static final List<String> FULL = List.of("IncursionStormwing", "IncursionStormwing", "IncursionStormwing", "IncursionStormwing");

    @Test
    void activeNeedsReportAndAllFourPieces() {
        assertTrue(BudgieCoreHook.active(Set.of("IncursionStormwing"), "IncursionStormwing", FULL));
        assertFalse(BudgieCoreHook.active(Set.of(), "IncursionStormwing", FULL), "the set hasn't been reported equipped");
        assertFalse(BudgieCoreHook.active(null, "IncursionStormwing", FULL));
        List<String> missing = Arrays.asList("IncursionStormwing", null, "IncursionStormwing", "IncursionStormwing");
        assertFalse(BudgieCoreHook.active(Set.of("IncursionStormwing"), "IncursionStormwing", missing), "a stale report after a bulk swap");
        List<String> mixed = List.of("IncursionStormwing", "Frostguard", "IncursionStormwing", "IncursionStormwing");
        assertFalse(BudgieCoreHook.active(Set.of("IncursionStormwing"), "IncursionStormwing", mixed));
        assertTrue(BudgieCoreHook.active(Set.of("IncursionStormwing"), "IncursionStormwing", List.of("incursionstormwing", "IncursionStormwing",
                "INCURSIONSTORMWING", "IncursionStormwing")), "tags match in any case, as the set check does");
    }

    EffectParser.ContentResult load(String yaml) throws Exception {
        Files.createDirectories(root.resolve("sets"));
        Files.writeString(root.resolve("sets/stormwing.yml"), yaml);
        return EffectParser.content(new ContentType("set", root.resolve("sets"), false, BudgieCoreHook::parse), Map.of());
    }

    static String errors(Problems p) {
        return String.join("\n", p.errors().stream().map(Problems.Problem::toString).toList());
    }

    @Test
    void parsesASet() throws Exception {
        var r = load("""
                set: IncursionStormwing
                display-name: "&bEye of the Storm"
                description: [Faster, Harder hits]
                effects:
                  - id: movement_speed_multiplier
                    args:
                      multiplier: 1.05
                  - id: damage_multiplier
                    args:
                      multiplier: 1.1
                    triggers: [melee_attack]
                """);
        assertEquals("", errors(r.problems()));
        var bonus = (BudgieCoreHook.SetBonus) r.holders().get("stormwing");
        assertEquals("set:stormwing", bonus.key(), "the key BudgieCore gave it, so stored cooldowns and charge carry on");
        assertEquals("IncursionStormwing", bonus.armorSet());
        assertEquals(List.of("Faster", "Harder hits"), bonus.description());
        assertEquals(2, bonus.effects().size());
    }

    @Test
    void needsSetAndKnownKeys() throws Exception {
        assertTrue(errors(load("effects: []\n").problems()).contains("needs set:"));
        assertTrue(errors(load("set: X\npieces: 2\n").problems()).contains("unknown set key 'pieces'"));
    }

    @Test
    void oldFilesCopyOnceAndStay() throws Exception {
        Path old = root.resolve("BudgieCore/sets"), now = root.resolve("BudgieSets/budgie/sets");
        Files.createDirectories(old.resolve("incursion"));
        Files.writeString(old.resolve("aged.yml"), "set: Aged\n");
        Files.writeString(old.resolve("incursion/stormwing.yml"), "set: IncursionStormwing\n");
        assertEquals(2, BudgieCoreHook.copyOldFiles(old, now));
        assertEquals("set: IncursionStormwing\n", Files.readString(now.resolve("incursion/stormwing.yml")));
        assertTrue(Files.exists(old.resolve("aged.yml")), "the originals stay as a backup");

        Files.writeString(now.resolve("aged.yml"), "set: Aged\ndisplay-name: Edited here\n");
        Files.writeString(old.resolve("new.yml"), "set: New\n");
        assertEquals(0, BudgieCoreHook.copyOldFiles(old, now), "once budgie/sets exists, nothing more comes over");
        assertEquals("set: Aged\ndisplay-name: Edited here\n", Files.readString(now.resolve("aged.yml")));
        assertFalse(Files.exists(now.resolve("new.yml")));
    }

    @Test
    void nothingToCopy() throws Exception {
        assertEquals(0, BudgieCoreHook.copyOldFiles(root.resolve("BudgieCore/sets"), root.resolve("BudgieSets/budgie/sets")));
        assertFalse(Files.exists(root.resolve("BudgieSets/budgie/sets")));
    }
}
