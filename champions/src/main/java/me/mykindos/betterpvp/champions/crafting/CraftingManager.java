package me.mykindos.betterpvp.champions.crafting;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;

import java.util.List;

@Singleton
public class CraftingManager {


    @Getter
    private List<Imbuement> imbuements;

    @Inject
    public CraftingManager(ImbuementRepository imbuementRepository) {
        imbuements = imbuementRepository.getAll();
    }


}