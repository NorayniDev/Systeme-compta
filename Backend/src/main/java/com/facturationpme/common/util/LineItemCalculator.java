package com.facturationpme.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Calcul des montants de lignes (devis, factures) - source de verite unique cote serveur, jamais
 * fait confiance aux totaux envoyes par le client (les DTO de creation/mise a jour n'exposent
 * d'ailleurs aucun champ de total, uniquement quantite/prix unitaire/taux de TVA par ligne).
 */
public final class LineItemCalculator {

  private static final int SCALE = 2;
  private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

  private LineItemCalculator() {}

  public record LineInput(BigDecimal quantity, BigDecimal unitPrice, BigDecimal taxRate) {}

  public record Totals(BigDecimal amountExclTax, BigDecimal taxAmount, BigDecimal totalAmount) {}

  public static BigDecimal computeLineTotal(LineInput line) {
    return line.quantity().multiply(line.unitPrice()).setScale(SCALE, RoundingMode.HALF_UP);
  }

  public static Totals computeTotals(List<LineInput> lines) {
    BigDecimal amountExclTax = BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
    BigDecimal taxAmount = BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);

    for (LineInput line : lines) {
      BigDecimal lineTotal = computeLineTotal(line);
      amountExclTax = amountExclTax.add(lineTotal);
      taxAmount =
          taxAmount.add(
              lineTotal.multiply(line.taxRate()).divide(ONE_HUNDRED, SCALE, RoundingMode.HALF_UP));
    }

    return new Totals(amountExclTax, taxAmount, amountExclTax.add(taxAmount));
  }
}
