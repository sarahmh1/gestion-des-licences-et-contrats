export interface ClientStats {
  client: string;
  nomProduit?: string;
  totalInterventionsCuratives: number;
  interventionsConsommees: number;
  interventionsNonConsommees: number;
  totalInterventionsPreventives: number;
  preventivesConsommees: number;
  preventivesNonConsommees: number;
}
