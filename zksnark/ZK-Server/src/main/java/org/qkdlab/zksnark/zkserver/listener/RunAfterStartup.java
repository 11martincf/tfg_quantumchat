package org.qkdlab.zksnark.zkserver.listener;

import org.qkdlab.zksnark.zkserver.utils.FileServerDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * RunAfterStartup
 *
 * Inicializa la clase "FileServerDatabase" del servidor
 */
@Component
public class RunAfterStartup {

    @Autowired
    FileServerDatabase fileServerDatabase;

    @EventListener(ApplicationReadyEvent.class)
    public void runAfterStartup() {
        try {
            fileServerDatabase.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}