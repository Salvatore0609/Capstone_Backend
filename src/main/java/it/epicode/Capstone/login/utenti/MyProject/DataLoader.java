package it.epicode.Capstone.login.utenti.MyProject;

import it.epicode.Capstone.login.utenti.MyProject.fasi.Fase;
import it.epicode.Capstone.login.utenti.MyProject.fasi.FaseRepository;
import it.epicode.Capstone.login.utenti.MyProject.fasi.tasks.Task;
import it.epicode.Capstone.login.utenti.MyProject.fasi.tasks.TaskRepository;
import it.epicode.Capstone.login.utenti.MyProject.fasi.tasks.steps.Step;
import it.epicode.Capstone.login.utenti.MyProject.fasi.tasks.steps.StepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner{

    private final FaseRepository faseRepo;
    private final TaskRepository taskRepo;
    private final StepRepository stepRepo;

    @Override
    public void run(String... args) throws Exception {
        if (faseRepo.count() > 0) return;

        List<Fase> fasi = List.of(
                new Fase(null, "FASE INIZIALE", 1, null),
                new Fase(null, "PROGETTAZIONE PRELIMINARE", 2, null),
                new Fase(null, "PROGETTAZIONE DEFINITIVA E AUTORIZZAZIONI", 3, null),
                new Fase(null, "PROGETTAZIONE ESECUTIVA & APPALTI", 4, null),
                new Fase(null, "DIREZIONE LAVORI & CANTIERE", 5, null),
                new Fase(null, "FINE LAVORI & AGGIORNAMENTI CATASTALI", 6, null)
        );
        faseRepo.saveAll(fasi);

        // ------------------------------------
        // FASE 1: FASE INIZIALE
        Fase fase1 = fasi.get(0);
        Task t1_1 = new Task(null, "Incontro con il cliente", null, fase1, null);
        Task t1_2 = new Task(null, "Analisi normativa e vincoli", null, fase1, null);
        Task t1_3 = new Task(null, "Studio fattibilità tecnica ed economica", null, fase1, null);
        Task t1_4 = new Task(null, "Rilievo e documentazione", null, fase1, null);
        taskRepo.saveAll(List.of(t1_1,t1_2,t1_3,t1_4));

        stepRepo.saveAll(List.of(
                new Step(null, "Definisci esigenze e obiettivi", "textarea", "Scrivi qui le esigenze e gli obiettivi…", null, t1_1),
                new Step(null, "Carica una prima analisi del budget", "file", null, ".pdf", t1_1),

                new Step(null, "Scegli l’area di interesse (collegamento al PRG)", "dropdown", null, null, t1_2),
                new Step(null, "Verifica vincoli paesaggistici, ambientali, storici", "static", null, null, t1_2),
                new Step(null, "Conformità alle Normative Edilizie e Antisismiche", "static", null, null, t1_2),

                new Step(null, "Analisi del lotto o dell’edificio esistente", "file", null, ".pdf,.dwg", t1_3),
                new Step(null, "Verifica conformità catastale", "link", "Portale SISTER", null, t1_3),
                new Step(null, "Carica un primo preventivo", "file", null, ".pdf", t1_3),

                new Step(null, "Rilievo metrico dell’area o edificio", "file", null, ".pdf,.dwg", t1_4),
                new Step(null, "Fotografie stato di fatto", "file", null, ".jpg,.png", t1_4),
                new Step(null, "Acquisizione di visure catastali e planimetrie", "file", null, ".pdf", t1_4)
        ));

        // ------------------------------------
        // FASE 2: PROGETTAZIONE PRELIMINARE
        Fase fase2 = fasi.get(1);
        Task t2_1 = new Task(null, "Sviluppo del Concept e Bozze", null, fase2, null);
        Task t2_2 = new Task(null, "Studio delle Alternative Progettuali", null, fase2, null);
        Task t2_3 = new Task(null, "Presentazione al Cliente", null, fase2, null);
        taskRepo.saveAll(List.of(t2_1,t2_2,t2_3));

        stepRepo.saveAll(List.of(
                new Step(null, "Realizzazione di schizzi e idee progettuali a mano libera o in digitale", "file", null, ".jpg,.png,.pdf", t2_1),
                new Step(null, "Elaborazione delle prime proposte planimetriche e volumetrie", "file", null, ".pdf,.dwg", t2_1),

                new Step(null, "Valutazione dei diversi materiali e finiture", "file,textarea", null, ".pdf", t2_2),
                new Step(null, "Analisi dell’efficienza energetica e delle soluzioni tecnologiche per gli impianti (elettrico, idraulico, HVAC)", "file", null, ".pdf", t2_2),

                new Step(null, "Raccogli feedback del cliente", "textarea", "Annota qui i commenti del cliente…", null, t2_3),
                new Step(null, "Adattamento e revisione delle idee progettuali in base alle osservazioni", "file", null, ".pdf,.jpg,.png,.dwg", t2_3)
        ));

        // ------------------------------------
        // FASE 3: PROGETTAZIONE DEFINITIVA E AUTORIZZAZIONI
        Fase fase3 = fasi.get(2);
        Task t3_1 = new Task(null, "Elaborazione degli Elaborati Tecnici", null, fase3, null);
        Task t3_2 = new Task(null, "Pratiche Burocratiche e Permessi", null, fase3, null);
        Task t3_3 = new Task(null, "Coordinamento Interdisciplinare", "Collaborazione con ingegneri strutturali, impiantisti e geologi", fase3, null);
        taskRepo.saveAll(List.of(t3_1,t3_2,t3_3));

        stepRepo.saveAll(List.of(
                new Step(null, "Carica piante, prospetti, sezioni e dettagli costruttivi", "file", null, ".pdf,.dwg", t3_1),
                new Step(null, "Carica il computo metrico estimativo", "file", null, ".xls,.xlsx,.pdf", t3_1),
                new Step(null, "Carica il capitolato tecnico con materiali e lavorazioni", "file", null, ".pdf", t3_1),

                new Step(null, "Documentazione per Permesso di Costruire o SCIA", "file", null, ".pdf", t3_2),
                new Step(null, "Conferma invio pratiche agli enti (Comune, ASL, etc.)", "boolean", null, null, t3_2),

                new Step(null, "Allega relazioni strutturali, geologiche o impiantistiche", "file", null, ".pdf,.docx", t3_3),
                new Step(null, "Note sugli aggiornamenti urbanistici e catastali", "textarea,file", "Inserisci note o aggiornamenti recenti…", ".pdf", t3_3),
                new Step(null, "Gestione sanatorie per abusi edilizi pregressi", "textarea,file", "Descrizione della sanatoria e documenti allegati…", ".pdf,.dwg", t3_3)
        ));

        // ------------------------------------
        // FASE 4: PROGETTAZIONE ESECUTIVA & APPALTI
        Fase fase4 = fasi.get(3);
        Task t4_1 = new Task(null, "Progettazione Esecutiva", null, fase4, null);
        Task t4_2 = new Task(null, "Preparazione del Computo Metrico Definitivo", null, fase4, null);
        Task t4_3 = new Task(null, "Scelta dell’Impresa Esecutrice", null, fase4, null);
        Task t4_4 = new Task(null, "Piano di Sicurezza e Coordinamento (PSC)", null, fase4, null);
        taskRepo.saveAll(List.of(t4_1,t4_2,t4_3,t4_4));

        stepRepo.saveAll(List.of(
                new Step(null, "Carica disegni esecutivi con misurazioni e dettagli", "file", null, ".pdf,.dwg", t4_1),
                new Step(null, "Definisci gli schemi per impianti e strutture", "file", null, ".pdf,.dwg", t4_1),

                new Step(null, "Carica computo metrico definitivo", "file", null, ".xls,.xlsx,.pdf", t4_2),
                new Step(null, "Note sui preventivi ricevuti dalle imprese", "textarea,file", "Inserisci considerazioni o allega documenti…", ".pdf", t4_2),

                new Step(null, "Carica valutazione e schede imprese selezionate", "file", null, ".pdf", t4_3),
                new Step(null, "Stipula del contratto d'appalto", "file", null, ".pdf", t4_3),

                new Step(null, "Carica PSC e documenti di sicurezza", "file", null, ".pdf", t4_4),
                new Step(null, "Nomina del Coordinatore per la Sicurezza", "file", null, ".pdf", t4_4)
        ));

        // ------------------------------------
        // FASE 5: DIREZIONE LAVORI & CANTIERE
        Fase fase5 = fasi.get(4);
        Task t5_1 = new Task(null, "Apertura del Cantiere", null, fase5, null);
        Task t5_2 = new Task(null, "Supervisione e Controllo dei Lavori", null, fase5, null);
        Task t5_3 = new Task(null, "Sicurezza in Cantiere", null, fase5, null);
        Task t5_4 = new Task(null, "Certificazioni e Collaudi", null, fase5, null);
        taskRepo.saveAll(List.of(t5_1, t5_2, t5_3, t5_4));

        stepRepo.saveAll(List.of(

                new Step(null, "Comunicazione dell’inizio dei lavori al Comune (CILA/SCIA)", "file", null, ".pdf", t5_1),
                new Step(null, "Nomina del Direttore dei Lavori", "file", null, ".pdf", t5_1),


                new Step(null, "Report di monitoraggio lavori in corso", "textarea,file", "Descrivi lo stato di avanzamento dei lavori...", ".pdf,.docx", t5_2),
                new Step(null, "Aggiornamento computo metrico (SAL)", "file", null, ".xls,.xlsx,.pdf", t5_2),


                new Step(null, "Verifica utilizzo DPI e misure di sicurezza", "boolean", null, null, t5_3),
                new Step(null, "Documentazione sul rispetto normative sicurezza (D.Lgs. 81/08)", "file", null, ".pdf", t5_3),


                new Step(null, "Collaudo strutturale e impiantistico", "file", null, ".pdf", t5_4),
                new Step(null, "Attestato di Prestazione Energetica (APE) e certificazioni varie", "file", null, ".pdf", t5_4)
        ));

        // ------------------------------------
        // FASE 6: FINE LAVORI & AGGIORNAMENTI CATASTALI
        Fase fase6 = fasi.get(5);
        Task t6_1 = new Task(null, "Comunicazione di Fine Lavori", null, fase6, null);
        Task t6_2 = new Task(null, "Aggiornamento Catastale e Richiesta di Agibilità", null, fase6, null);
        Task t6_3 = new Task(null, "Consegna del Progetto al Cliente", null, fase6, null);
        taskRepo.saveAll(List.of(t6_1, t6_2, t6_3));

        stepRepo.saveAll(List.of(

                new Step(null, "Notifica ufficiale di fine lavori al Comune", "file", null, ".pdf", t6_1),
                new Step(null, "Deposito della documentazione finale", "file", null, ".pdf,.zip", t6_1),


                new Step(null, "Documento di aggiornamento catastale", "file", null, ".pdf,.xml", t6_2),
                new Step(null, "Richiesta di certificazione di agibilità", "file", null, ".pdf", t6_2),


                new Step(null, "Documentazione tecnica e legale", "file", null, ".pdf,.zip", t6_3),
                new Step(null, "Manuali d’uso, manutenzione e garanzie", "file", null, ".pdf,.zip", t6_3)
        ));


        System.out.println("fasi, task e step COMPLETATO!");
    }

}
