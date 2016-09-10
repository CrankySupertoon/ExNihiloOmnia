package exnihiloomnia;

import net.minecraftforge.common.config.Configuration;

public class ENOConfig {
    public static boolean classic_sieve;
    public static double string_chance;
    public static boolean sieve_automation;

    public static void configure(Configuration config) {
        classic_sieve = config.get("legacy options", "enable classic sieve", false).getBoolean(false);
        string_chance = config.get("misc", "infested leaves string chance", .6d).getDouble();

        sieve_automation = config.get("misc", "sieve hopper/pipe interaction", false).getBoolean(false);
    }
}