import { CommandePasserPar } from "./CommandePasserPar";
import { Duree } from "./Duree";

export class Fortinet {
    fortinetId: number;
    client: string;
    nomDuBoitier: string;
    numeroSerie: string;
    dureeDeLicence: Duree;
    nomDuContact: string;
    commandePasserPar:CommandePasserPar; 
    adresseEmailContact: string;
    mailAdmin: string;
    ccMail: string[];
    numero: string;
    approuve?: boolean;
    sousContrat: boolean;
    remarque: string;
    fichier?: string;
    fichierOriginalName?: string;
    licences: {
      nomDesLicences: string;
      quantite: string;
      dateEx: Date;
}[];

    
}
