package com.facturationpme.pdf.service;

/** Contenu binaire du PDF genere, accompagne du nom de fichier suggere au telechargement. */
public record GeneratedPdf(byte[] content, String fileName) {}
