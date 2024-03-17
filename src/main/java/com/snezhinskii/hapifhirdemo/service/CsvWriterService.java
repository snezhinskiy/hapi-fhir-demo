package com.snezhinskii.hapifhirdemo.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Log4j2
@Component
public class CsvWriterService {

    public void write(List<List<String>> data, String path) {
        if (StringUtils.hasText(path)) {
            log.debug("Try to save data into file save: {}", path);

            try (FileWriter writer = new FileWriter(path)) {
                for (List<String> row : data) {
                    StringBuilder sb = new StringBuilder();

                    int cellNumber = 0;

                    for (String cell : row) {
                        if (cellNumber > 0) {
                            sb.append(",");
                        }
                        sb.append(escape(cell));

                        cellNumber++;
                    }

                    sb.append("\r\n");
                    writer.write(sb.toString());
                }

                log.debug("File {} saved", path);
            } catch (IOException e) {
                log.error("IO Error occured during csv save: {}", e);
            } catch (Exception e) {
                log.error("Error occured during csv save: {}", e);
            }
        }
    }

    private String escape(String input) {
        if (StringUtils.hasText(input)
            && (input.contains(",") || input.contains("\""))
        ) {
            return "'" + input + "'";
        }

        return input;
    }
}
