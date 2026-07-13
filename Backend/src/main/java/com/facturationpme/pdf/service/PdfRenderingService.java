package com.facturationpme.pdf.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Coeur generique du rendu PDF : gabarit Thymeleaf (classpath:/templates/{template}.html) ->
 * document HTML -> PDF via openhtmltopdf. Les services specifiques ({@code InvoicePdfService},
 * {@code QuotePdfService}, {@code PaymentReceiptPdfService}) se contentent d'assembler les
 * variables de contexte ; toute la mecanique de rendu est concentree ici.
 *
 * <p>openhtmltopdf attend du XML strictement bien forme si on lui passe une chaine HTML brute - un
 * gabarit Thymeleaf, meme correct, n'offre aucune garantie absolue en ce sens. On repasse donc par
 * Jsoup (analyseur HTML tolerant) pour forcer une serialisation XHTML bien formee, convertie en DOM
 * W3C et transmise directement a openhtmltopdf sans nouvelle analyse.
 */
@Service
@RequiredArgsConstructor
public class PdfRenderingService {

  private final TemplateEngine templateEngine;

  public byte[] render(String templateName, Map<String, Object> variables) {
    Context context = new Context();
    context.setVariables(variables);
    String html = templateEngine.process(templateName, context);

    Document jsoupDocument = Jsoup.parse(html);
    jsoupDocument.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
    jsoupDocument.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
    org.w3c.dom.Document w3cDocument = new W3CDom().fromJsoup(jsoupDocument);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try {
      PdfRendererBuilder builder = new PdfRendererBuilder();
      builder.useFastMode();
      builder.withW3cDocument(w3cDocument, "");
      builder.toStream(outputStream);
      builder.run();
    } catch (Exception ex) {
      throw new IllegalStateException(
          "Echec de generation du PDF (gabarit " + templateName + ")", ex);
    }
    return outputStream.toByteArray();
  }
}
