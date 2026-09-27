package nadiendev.kubejsastralsorcery.network;

import hellfirepvp.astralsorcery.client.screen.tome.TomeResearchScreen;

public final class ClientHooks {
    private ClientHooks() {
    }

    public static void resetTome() {
        TomeResearchScreen.resetOpenTome();
    }
}
