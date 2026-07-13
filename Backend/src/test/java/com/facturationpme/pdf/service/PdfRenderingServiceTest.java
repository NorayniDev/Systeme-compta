package com.facturationpme.pdf.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * N'utilise jamais {@code PdfRenderingService} mocke : ces tests generent un vrai PDF via un vrai
 * moteur Thymeleaf + openhtmltopdf, puis en extraient le texte via PDFBox pour verifier le contenu
 * - la seule facon de detecter un gabarit casse (XML mal forme, expression SpringEL invalide) sans
 * pouvoir visualiser le rendu.
 */
class PdfRenderingServiceTest {

  private final PdfRenderingService pdfRenderingService = new PdfRenderingService(templateEngine());

  private static TemplateEngine templateEngine() {
    ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
    resolver.setTemplateMode(TemplateMode.HTML);
    resolver.setCharacterEncoding("UTF-8");
    resolver.setSuffix(".html");
    resolver.setPrefix("templates/");
    // SpringTemplateEngine (pas TemplateEngine brut) : enregistre SpringStandardDialect
    // (SpringEL), le meme moteur utilise en production via spring-boot-starter-thymeleaf. Le
    // dialecte OGNL par defaut de TemplateEngine necessiterait une dependance ognl supplementaire
    // que la production n'a jamais besoin d'ajouter.
    TemplateEngine engine = new SpringTemplateEngine();
    engine.setTemplateResolver(resolver);
    return engine;
  }

  @Test
  void renderShouldProduceAValidPdfContainingTheTemplateVariable() throws Exception {
    byte[] pdf =
        pdfRenderingService.render(
            "pdf-test/minimal", Map.of("greeting", "Bonjour FacturationPME"));

    assertThat(pdf).isNotEmpty();
    assertThat(new String(pdf, 0, 4, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF");

    try (PDDocument document = PDDocument.load(pdf)) {
      String text = new PDFTextStripper().getText(document);
      assertThat(text).contains("Bonjour FacturationPME");
    }
  }
}
