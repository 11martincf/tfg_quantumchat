package org.qkdlab.zksnark.zkserver.controller;

import org.qkdlab.zksnark.model.Constants;
import org.qkdlab.zksnark.zkserver.utils.FileServerDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * MerkleTreeController
 *
 * Endpoints para el acciones relacionadas con el Merkle Tree
 * Concretamente, obtención del fichero "tree.json" que contiene el árbol, o la lista de roots antiguos
 */
@Controller
@RequestMapping(value = "/tree")
public class MerkleTreeController {

    @Autowired
    private FileServerDatabase fileServerDatabase;

    /**
     * Descarga el árbol Merkle
     * @return fichero "tree.json" para ser descargado por el cliente
     */
    @GetMapping(path = "/download")
    public ResponseEntity<Resource> download() {

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + Constants.DEFAULT_TREE_FILENAME);
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        File file = null;
        ByteArrayResource resource;
        try {
            file = fileServerDatabase.getTreeFile();
            Path path = Paths.get(file.getAbsolutePath());
            resource = new ByteArrayResource(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /**
     * Solicita lista de roots
     * @return Lista de roots de estados anteriores del árbol Merkle
     */
    @GetMapping(path = "/roots")
    public ResponseEntity<List<byte[]>> getTreeRoots() {
        List<byte[]> roots = fileServerDatabase.getMerkleRoots();
        return new ResponseEntity<>(roots, HttpStatus.OK);
    }
}
