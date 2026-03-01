import { NomProduit } from "./NomProduit";
import { TypeAchat } from "./TypeAchat";
import { Duree } from "./Duree";
import { CommandePasserPar } from 'app/Model/CommandePasserPar';
//import { TypeAchat } from './'
export class Eset
{
  
  esetid: number;            
      client: string;           
      identifiant: string;   
      //**** */    
      cle_de_Licence: string;      
      nom_produit:NomProduit;        
      nombre: number; 
      commandePasserPar:CommandePasserPar;            
      nmb_tlf: number;
      sousContrat: boolean;
      remarque: string;            
      nom_contact: string;
      mail: string;                      
      mailAdmin: string;   
      dureeDeLicence: Duree;      
      dateEx: Date;              
      typeAchat:TypeAchat;      
      approuve?:Boolean; 
      ccMail: string[];
      fichier?: string;
      fichierOriginalName?: string;
    }
    

