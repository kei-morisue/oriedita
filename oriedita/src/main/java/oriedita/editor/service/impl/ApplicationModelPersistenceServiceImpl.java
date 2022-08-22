package oriedita.editor.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.tinylog.Logger;
import oriedita.editor.databinding.ApplicationModel;
import oriedita.editor.json.DefaultObjectMapper;
import oriedita.editor.service.ApplicationModelPersistenceService;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static oriedita.util.DirectoryUtil.getAppDir;

@Singleton
public class ApplicationModelPersistenceServiceImpl implements ApplicationModelPersistenceService {

    public static final String CONFIG_JSON = "config.json";
    private final JFrame frame;
    private final ApplicationModel applicationModel;
    private final ObjectMapper mapper;

    @Inject
    public ApplicationModelPersistenceServiceImpl(@Named("mainFrame") JFrame frame, ApplicationModel applicationModel) {
        this.frame = frame;
        this.applicationModel = applicationModel;
        mapper = new DefaultObjectMapper();
    }

    @Override public void init() {
        restoreApplicationModel();
        applicationModel.addPropertyChangeListener(e -> persistApplicationModel());
    }

    public void restoreApplicationModel() {
        Path storage = getAppDir();
        File configFile = storage.resolve(CONFIG_JSON).toFile();

        if (!configFile.exists()) {
            applicationModel.reset();

            return;
        }

        try {
            ApplicationModel loadedApplicationModel = mapper.readValue(configFile, ApplicationModel.class);

            applicationModel.set(loadedApplicationModel);
        } catch (IOException e) {
            // An application state is found, but it is not valid.
            JOptionPane.showMessageDialog(frame, "<html>Failed to load application state.<br/>Loading default application configuration.", "State load failed", JOptionPane.WARNING_MESSAGE);

            if (!configFile.renameTo(storage.resolve(CONFIG_JSON + ".old").toFile())) {
                Logger.error("Not allowed to move config.json");
            }

            applicationModel.reset();
        }
    }

    public void persistApplicationModel() {
        Path storage = getAppDir();

        if (!storage.toFile().exists()) {
            if (!storage.toFile().mkdirs()) {
                Logger.error("Failed to create directory for application model");

                return;
            }
        }

        ObjectMapper mapper = new DefaultObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            mapper.writeValue(storage.resolve(CONFIG_JSON).toFile(), applicationModel);
        } catch (IOException e) {
            Logger.error(e, "Unable to write applicationModel to disk.");
        }
    }
}
