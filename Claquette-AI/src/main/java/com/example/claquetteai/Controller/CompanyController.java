package com.example.claquetteai.Controller;

import com.example.claquetteai.Api.ApiResponse;
import com.example.claquetteai.DTO.CompanyDTO;
import com.example.claquetteai.Service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/get")
    public ResponseEntity<?> getAllCompanies() {
        return ResponseEntity.ok().body(companyService.getAllCompanies());
    }

    @PostMapping("/add")
    public ResponseEntity<?> addCompany(@RequestBody @Valid CompanyDTO dto) {
        companyService.addCompany(dto);
        return ResponseEntity.ok().body(new ApiResponse("Company has been added successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateCompany(@PathVariable Integer id,
                                                     @RequestBody @Valid CompanyDTO dto) {
        companyService.updateCompany(id, dto);
        return ResponseEntity.ok().body(new ApiResponse("Company has been updated successfully"));
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteCompany(@PathVariable Integer id) {
        companyService.deleteCompany(id);
        return ResponseEntity.ok().body(new ApiResponse("Company has been deleted successfully"));
    }

}
