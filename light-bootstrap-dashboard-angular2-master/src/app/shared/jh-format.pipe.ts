import { Pipe, PipeTransform } from '@angular/core';

/**
 * Formate une valeur décimale en J/H vers un affichage lisible.
 * 1 J/H = 8 heures.
 *
 * Exemples :
 *   10     → "10J"
 *   0.25   → "2h"
 *   9.75   → "9J 6h"
 *   1.5    → "1J 4h"
 *   0      → "0J"
 */
@Pipe({ name: 'jhFormat' })
export class JhFormatPipe implements PipeTransform {
  transform(value: number): string {
    if (value == null || isNaN(value)) return '0J';

    const totalHours = Math.round(value * 8 * 100) / 100; // convert J/H to hours, avoid floating point issues
    const jours = Math.floor(totalHours / 8);
    const heures = Math.round(totalHours % 8);

    if (jours > 0 && heures > 0) {
      return `${jours}J ${heures}h`;
    } else if (jours > 0) {
      return `${jours}J`;
    } else if (heures > 0) {
      return `${heures}h`;
    }
    return '0J';
  }
}
