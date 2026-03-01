import { CommandePasserPar } from "./CommandePasserPar";

export class Varonis {
    varonisId: number;
    client: string;
    dureeDeLicence: string;
    nomDuContact: string;
    adresseEmailContact: string;
    mailAdmin: string;
    ccMail: string[];
    numero: string;
    commandePasserPar: CommandePasserPar;
    approuve?: boolean;
    remarque: string;
    sousContrat: boolean;
    fichier?: string;
    fichierOriginalName?: string;
     licences: {
      nomDesLicences: string;
      quantite: string;
      dateEx: Date;
}[];
}
