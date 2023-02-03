package comptoirs.service;

import comptoirs.dao.CommandeRepository;
import comptoirs.dao.LigneRepository;
import comptoirs.dao.ProduitRepository;
import comptoirs.entity.Ligne;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Positive;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated // Les contraintes de validatipn des méthodes sont vérifiées
public class LigneService {
    // La couche "Service" utilise la couche "Accès aux données" pour effectuer les traitements
    private final CommandeRepository commandeDao;
    private final LigneRepository ligneDao;
    private final ProduitRepository produitDao;

    // @Autowired
    // La couche "Service" utilise la couche "Accès aux données" pour effectuer les traitements
    public LigneService(CommandeRepository commandeDao, LigneRepository ligneDao, ProduitRepository produitDao) {
        this.commandeDao = commandeDao;
        this.ligneDao = ligneDao;
        this.produitDao = produitDao;
    }

    /**
     * <pre>
     * Service métier : 
     *     Enregistre une nouvelle ligne de commande pour une commande connue par sa clé,
     *     Incrémente la quantité totale commandée (Produit.unitesCommandees) avec la quantite à commander
     * Règles métier :
     *     - le produit référencé doit exister et être disponible
     *     - la commande doit exister
     *     - la commande ne doit pas être déjà envoyée (le champ 'envoyeele' doit être null)
     *     - la quantité doit être positive
     *     - On doit avoir une quantite en stock du produit suffisante
     * <pre>
     * 
     *  @param commandeNum la clé de la commande
     *  @param produitRef la clé du produit
     *  @param quantite la quantité commandée (positive)
     *  @return la ligne de commande créée
     */
    @Transactional
    Ligne ajouterLigne(Integer commandeNum, Integer produitRef, @Positive int quantite) {
        var commande = commandeDao.findById(commandeNum).orElseThrow();
        var produit = produitDao.findById(produitRef).orElseThrow();
        // La commande doit exister
        if (commande == null) {
            throw new IllegalArgumentException("Commande inconnue");
        }
        // La commande ne doit pas être déjà envoyée
        if (commande.getEnvoyeele() != null) {
            throw new IllegalArgumentException("Commande déjà envoyée");
        }
        // Le produit doit exister et être disponible
        if (produit == null || produit.getIndisponible()) {
            throw new IllegalArgumentException("Produit indisponible");
        }
        // Il faut avoir une quantite en stock du produit suffisante
        if (produit.getUnitesEnStock() < quantite) {
            throw new IllegalArgumentException("Quantite en stock insuffisante");
        }
        // Incrémente la quantité totale commandée (Produit.unitesCommandees) avec la quantite à commander
        produit.setUnitesCommandees(produit.getUnitesCommandees() + quantite);
        // Enregistre la ligne de commande
        var ligne = new Ligne();
        ligne.setCommande(commande);
        ligne.setProduit(produit);
        ligne.setQuantite(quantite);
        ligneDao.save(ligne);
        return ligne;
    }
}
