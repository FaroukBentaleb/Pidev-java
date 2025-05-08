package tn.learniverse.tests;

import tn.learniverse.entities.Commentaire;
import tn.learniverse.entities.Poste;
import tn.learniverse.entities.User;
import tn.learniverse.services.CommentaireService;
import tn.learniverse.services.PosteService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


import java.util.List;
import java.util.Scanner;

public class MainForum {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        PosteService posteService = new PosteService();

        CommentaireService commentaireService = new CommentaireService();

        while (true) {
            System.out.println("\n=== MENU FORUM ===");
            System.out.println("1. Ajouter un poste");
            System.out.println("2. Afficher tous les postes");
            System.out.println("3. Supprimer un poste");
            System.out.println("4. Ajouter un commentaire");
            System.out.println("5. Afficher tous les commentaires");
            System.out.println("6. Supprimer un commentaire");
            System.out.println("7. Modifier un poste");
            System.out.println("8. Modifier un commentaire");

            System.out.println("0. Quitter");
            System.out.print("Choix : ");
            int choix = sc.nextInt();
            sc.nextLine(); // consommer le retour à la ligne

            switch (choix) {
                case 1 -> {
                    Poste p = new Poste();
                    User u = new User();
                    p.setUser(u);
                    System.out.print("Titre : ");
                    p.setTitre(sc.nextLine());
                    System.out.print("Contenu : ");
                    p.setContenu(sc.nextLine());
                    System.out.print("Catégorie : ");
                    p.setCategorie(sc.nextLine());
                    System.out.print("Photo : ");
                    p.setPhoto(sc.nextLine());

                    posteService.ajouter(p);
                }

                case 2 -> {
                    List<Poste> postes = posteService.getAll();
                    System.out.println("\n📋 Liste des Postes :");
                    for (Poste p : postes) {
                        System.out.println(p);
                    }
                }

                case 3 -> {
                    System.out.print("ID du poste à supprimer : ");
                    int id = sc.nextInt();
                    posteService.supprimer(id);
                }

                case 4 -> {
                    Commentaire c = new Commentaire();
                    User u = new User();
                    Poste p = new Poste();

                    System.out.print("ID du poste : ");
                    p.setId(sc.nextInt());
                    sc.nextLine(); // consume newline
                    c.setUser(u);
                    c.setPoste(p);
                    System.out.print("Contenu : ");
                    c.setContenu(sc.nextLine());
                    commentaireService.ajouter(c);
                }

                case 5 -> {
                    List<Commentaire> commentaires = commentaireService.getAll();
                    System.out.println("\n💬 Liste des Commentaires :");
                    for (Commentaire c : commentaires) {
                        System.out.println(c);
                    }
                }

                case 6 -> {
                    System.out.print("ID du commentaire à supprimer : ");
                    int id = sc.nextInt();
                    commentaireService.supprimer(id);
                }

                case 0 -> {
                    System.out.println("👋 Au revoir !");
                    return;
                }

                case 7 -> {
                    System.out.print("ID du poste à modifier : ");
                    int idPoste = sc.nextInt();
                    sc.nextLine(); // consommer le retour à la ligne

                    Poste p = posteService.getById(idPoste);
                    if (p == null) {
                        System.out.println("❌ Poste non trouvé !");
                        break;
                    }

                    System.out.println("Laissez vide pour ne pas modifier le champ.");

                    System.out.print("Nouveau titre (actuel : " + p.getTitre() + ") : ");
                    String titre = sc.nextLine();
                    if (!titre.isEmpty()) p.setTitre(titre);

                    System.out.print("Nouveau contenu (actuel : " + p.getContenu() + ") : ");
                    String contenu = sc.nextLine();
                    if (!contenu.isEmpty()) p.setContenu(contenu);

                    System.out.print("Nouvelle catégorie (actuelle : " + p.getCategorie() + ") : ");
                    String cat = sc.nextLine();
                    if (!cat.isEmpty()) p.setCategorie(cat);

                    System.out.print("Nouvelle photo (actuelle : " + p.getPhoto() + ") : ");
                    String photo = sc.nextLine();
                    if (!photo.isEmpty()) p.setPhoto(photo);

                    posteService.modifier(p);
                }


                case 8 -> {
                    System.out.print("ID du commentaire à modifier : ");
                    int idCom = sc.nextInt();
                    sc.nextLine();

                    Commentaire c = commentaireService.getById(idCom);
                    if (c == null) {
                        System.out.println("❌ Commentaire non trouvé !");
                        break;
                    }

                    System.out.println("Laissez vide pour ne pas modifier le champ.");

                    System.out.print("Nouveau contenu (actuel : " + c.getContenu() + ") : ");
                    String contenu = sc.nextLine();
                    if (!contenu.isEmpty()) c.setContenu(contenu);

                    System.out.print("Nouvelle URL du GIF (actuelle : " + c.getGifurl() + ") : ");
                    String gif = sc.nextLine();
                    if (!gif.isEmpty()) c.setGifurl(gif);

                    // On garde visible true et date = maintenant (optionnel)
                    c.setVisible(true);
                    c.setDateComment(LocalDate.now().toString());

                    commentaireService.modifier(c);
                }



                default -> System.out.println("❌ Choix invalide !");
            }
        }
    }
}
