package com.maxwell.qliphoth_armaments.init;

import com.finderfeed.fdbosses.content.entities.geburah.sins.attachment.PlayerSin;
import com.finderfeed.fdbosses.init.BossRegistries;
import com.maxwell.qliphoth_armaments.QA;
import com.maxwell.qliphoth_armaments.common.sins.*;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModSins {
    public static final DeferredRegister<PlayerSin> SINS = DeferredRegister.create(BossRegistries.PLAYER_SIN, QA.MOD_ID);
    public static final Supplier<WrathSin> WRATH = SINS.register("wrath", WrathSin::new);
    public static final Supplier<SilenceSin> SILENCE = SINS.register("silence", SilenceSin::new);
    public static final Supplier<PrideSin> PRIDE = SINS.register("pride", PrideSin::new);
    public static final Supplier<EnvySin> ENVY = SINS.register("envy", EnvySin::new);
    public static final Supplier<ThirstSin> THIRST = SINS.register("thirst", ThirstSin::new);
    public static final Supplier<RestlessSin> RESTLESS = SINS.register("restless", RestlessSin::new);
    public static final Supplier<HubrisSin> HUBRIS = SINS.register("hubris", HubrisSin::new);
    public static final Supplier<VoracitySin> VORACITY = SINS.register("voracity", VoracitySin::new);
}