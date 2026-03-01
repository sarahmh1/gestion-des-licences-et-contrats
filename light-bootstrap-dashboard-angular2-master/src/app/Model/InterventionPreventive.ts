export interface IntervenantPreventif {
    intervenantPreventifId?: number;
    nom: string;
}

export interface PeriodeLigne {
    periodeLigneId?: number;
    periodeDe?: string;
    periodeA?: string;
    periodeRecommandeDe?: string;
    periodeRecommandeA?: string;
    dateInterventionExigee?: string;
    dateIntervention?: string;
    dateRapportPreventive?: string;
}

export enum StatutInterventionPreventive {
    CREE = 'CREE',
    EN_ATTENTE_INTERVENTION = 'EN_ATTENTE_INTERVENTION',
    TERMINE = 'TERMINE'
}

export interface InterventionPreventive {
    interventionPreventiveId?: number;
    nomClient: string;
    nbInterventionsParAn: number;
    periodeDe: string;
    periodeA: string;
    periodeRecommandeDe: string;
    periodeRecommandeA: string;
    dateInterventionExigee: string;
    dateIntervention: string;
    dateRapportPreventive: string;
    intervenants: IntervenantPreventif[];
    periodeLignes?: PeriodeLigne[];
    contrat?: any;
    fichier?: string;
    fichierOriginalName?: string;
    statut?: StatutInterventionPreventive;
    emailCommercial?: string;
    ccMail?: string[];
}
