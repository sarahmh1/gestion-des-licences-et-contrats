import { CommandePasserPar } from "./CommandePasserPar";

export class MicrosoftO365 {
    microsoftO365Id: number;
    client: string;
   licences: {
      nomDesLicences: string;
      quantite: string;
      dateEx: Date;
}[];
    dureeDeLicence: string;
    nomDuContact: string;
    adresseEmailContact: string;
    mailAdmin: string;
    ccMail: string[];
    commandePasserPar: CommandePasserPar;
    numero: string;
    approuve?: boolean;
    sousContrat: boolean;
    remarque: string;
    fichier?: string;
    fichierOriginalName?: string;
    }
