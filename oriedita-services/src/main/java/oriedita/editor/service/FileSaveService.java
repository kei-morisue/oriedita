package oriedita.editor.service;

import oriedita.editor.exception.FileReadingException;
import oriedita.editor.save.Save;

import java.io.File;

public interface FileSaveService {
    void openFile(File file) throws FileReadingException;

    void openFile();

    void importFile();

    void exportFile();

    void writeImageFile(File file);

    File selectSaveFile();

    File selectImportFile();

    File selectExportFile();

    Save readImportFile(File file) throws FileReadingException;

    Save readImportFile(File file, boolean askOnUnknownFormat) throws FileReadingException;

    void saveFile();

    void saveAsFile();

    boolean readBackgroundImageFromFile();

    void initAutoSave();
}
