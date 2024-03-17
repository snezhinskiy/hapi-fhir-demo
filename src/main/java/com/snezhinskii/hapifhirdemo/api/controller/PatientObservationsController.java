package com.snezhinskii.hapifhirdemo.api.controller;

import com.snezhinskii.hapifhirdemo.api.validation.FhirPatientId;
import com.snezhinskii.hapifhirdemo.service.PatientObservationsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/patient-observation/")
public class PatientObservationsController {
    private final PatientObservationsService observationsService;


    @GetMapping(path = "/{patientId}/retrieve")
    public CompletableFuture<List<Observation>> retrieveAndExportPatientObservationsAsync(
        @PathVariable @Valid @FhirPatientId String patientId, @RequestParam(required = false) String csvFilePath
    ) {
        return observationsService.retrieveAndExport(patientId, csvFilePath);
    }
}
