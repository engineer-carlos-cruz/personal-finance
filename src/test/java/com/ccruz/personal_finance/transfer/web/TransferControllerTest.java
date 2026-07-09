package com.ccruz.personal_finance.transfer.web;

import com.ccruz.personal_finance.account.persistence.Account;
import com.ccruz.personal_finance.transfer.persistence.Transfer;
import com.ccruz.personal_finance.transfer.service.TransferService;
import com.ccruz.personal_finance.transfer.web.dto.TransferResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransferController - Standalone")
class TransferControllerTest {

    @Mock
    private TransferService transferService;

    @Mock
    private TransferMapper transferMapper;

    @InjectMocks
    private TransferController transferController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transferController).build();
    }

    @Test
    @DisplayName("findAll should return 200 with list of transfers")
    void findAll_shouldReturn200WithTransfers() throws Exception {
        var source = Account.builder().id(1L).code("CASH").build();
        var target = Account.builder().id(2L).code("BANK").build();
        var transfer = Transfer.builder()
                .id(1L)
                .sourceAccount(source)
                .targetAccount(target)
                .amount(new BigDecimal("100.00"))
                .date(LocalDate.of(2026, 7, 9))
                .build();
        var response = new TransferResponse(1L, 1L, "CASH", 2L, "BANK", new BigDecimal("100.00"), LocalDate.of(2026, 7, 9));

        when(transferService.findAll()).thenReturn(List.of(transfer));
        when(transferMapper.toResponse(anyList())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/transfers").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].sourceAccountId").value(1))
                .andExpect(jsonPath("$[0].sourceAccountCode").value("CASH"))
                .andExpect(jsonPath("$[0].targetAccountId").value(2))
                .andExpect(jsonPath("$[0].targetAccountCode").value("BANK"))
                .andExpect(jsonPath("$[0].amount").value(100.00))
                .andExpect(jsonPath("$[0].date").value("2026-07-09"));

        verify(transferService).findAll();
    }

    @Test
    @DisplayName("findAll should return 200 with empty list when no transfers exist")
    void findAll_shouldReturn200WithEmptyList() throws Exception {
        when(transferService.findAll()).thenReturn(List.of());
        when(transferMapper.toResponse(anyList())).thenReturn(List.of());

        mockMvc.perform(get("/api/transfers").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));

        verify(transferService).findAll();
    }

    @Test
    @DisplayName("findById should return 200 with transfer when found")
    void findById_shouldReturn200WhenFound() throws Exception {
        var source = Account.builder().id(1L).code("CASH").build();
        var target = Account.builder().id(2L).code("BANK").build();
        var transfer = Transfer.builder()
                .id(1L)
                .sourceAccount(source)
                .targetAccount(target)
                .amount(new BigDecimal("100.00"))
                .date(LocalDate.of(2026, 7, 9))
                .build();
        var response = new TransferResponse(1L, 1L, "CASH", 2L, "BANK", new BigDecimal("100.00"), LocalDate.of(2026, 7, 9));

        when(transferService.findById(1L)).thenReturn(transfer);
        when(transferMapper.toResponse(transfer)).thenReturn(response);

        mockMvc.perform(get("/api/transfers/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sourceAccountId").value(1))
                .andExpect(jsonPath("$.sourceAccountCode").value("CASH"))
                .andExpect(jsonPath("$.targetAccountId").value(2))
                .andExpect(jsonPath("$.targetAccountCode").value("BANK"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.date").value("2026-07-09"));

        verify(transferService).findById(1L);
    }

    @Test
    @DisplayName("findById should return 404 when not found")
    void findById_shouldReturn404WhenNotFound() throws Exception {
        when(transferService.findById(anyLong()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found"));

        mockMvc.perform(get("/api/transfers/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(transferService).findById(99L);
    }

    @Test
    @DisplayName("create should return 201 with location header and created transfer")
    void create_shouldReturn201WithLocationAndBody() throws Exception {
        var source = Account.builder().id(1L).code("CASH").build();
        var target = Account.builder().id(2L).code("BANK").build();
        var created = Transfer.builder()
                .id(1L)
                .sourceAccount(source)
                .targetAccount(target)
                .amount(new BigDecimal("100.00"))
                .date(LocalDate.of(2026, 7, 9))
                .build();
        var response = new TransferResponse(1L, 1L, "CASH", 2L, "BANK", new BigDecimal("100.00"), LocalDate.of(2026, 7, 9));

        when(transferService.create(any())).thenReturn(created);
        when(transferMapper.toResponse(created)).thenReturn(response);

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceAccountId\":1,\"targetAccountId\":2,\"amount\":100.00,\"date\":\"2026-07-09\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/transfers/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sourceAccountId").value(1))
                .andExpect(jsonPath("$.sourceAccountCode").value("CASH"))
                .andExpect(jsonPath("$.targetAccountId").value(2))
                .andExpect(jsonPath("$.targetAccountCode").value("BANK"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.date").value("2026-07-09"));

        verify(transferService).create(any());
    }

    @Test
    @DisplayName("create should return 404 when source account not found")
    void create_shouldReturn404WhenSourceAccountNotFound() throws Exception {
        when(transferService.create(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Source account not found"));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceAccountId\":99,\"targetAccountId\":2,\"amount\":100.00,\"date\":\"2026-07-09\"}"))
                .andExpect(status().isNotFound());

        verify(transferService).create(any());
    }

    @Test
    @DisplayName("create should return 404 when target account not found")
    void create_shouldReturn404WhenTargetAccountNotFound() throws Exception {
        when(transferService.create(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Target account not found"));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceAccountId\":1,\"targetAccountId\":99,\"amount\":100.00,\"date\":\"2026-07-09\"}"))
                .andExpect(status().isNotFound());

        verify(transferService).create(any());
    }

    @Test
    @DisplayName("create should return 400 when source and target are the same")
    void create_shouldReturn400WhenSourceAndTargetAreSame() throws Exception {
        when(transferService.create(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Source and target accounts must be different"));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceAccountId\":1,\"targetAccountId\":1,\"amount\":100.00,\"date\":\"2026-07-09\"}"))
                .andExpect(status().isBadRequest());

        verify(transferService).create(any());
    }

    @Test
    @DisplayName("update should return 200 with updated transfer")
    void update_shouldReturn200WithUpdatedTransfer() throws Exception {
        var source = Account.builder().id(1L).code("CASH").build();
        var target = Account.builder().id(2L).code("BANK").build();
        var updated = Transfer.builder()
                .id(1L)
                .sourceAccount(source)
                .targetAccount(target)
                .amount(new BigDecimal("200.00"))
                .date(LocalDate.of(2026, 7, 10))
                .build();
        var response = new TransferResponse(1L, 1L, "CASH", 2L, "BANK", new BigDecimal("200.00"), LocalDate.of(2026, 7, 10));

        when(transferService.update(anyLong(), any())).thenReturn(updated);
        when(transferMapper.toResponse(updated)).thenReturn(response);

        mockMvc.perform(put("/api/transfers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceAccountId\":1,\"targetAccountId\":2,\"amount\":200.00,\"date\":\"2026-07-10\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(200.00))
                .andExpect(jsonPath("$.date").value("2026-07-10"));

        verify(transferService).update(anyLong(), any());
    }

    @Test
    @DisplayName("update should return 404 when transfer not found")
    void update_shouldReturn404WhenNotFound() throws Exception {
        when(transferService.update(anyLong(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found"));

        mockMvc.perform(put("/api/transfers/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceAccountId\":1,\"targetAccountId\":2,\"amount\":100.00,\"date\":\"2026-07-09\"}"))
                .andExpect(status().isNotFound());

        verify(transferService).update(anyLong(), any());
    }

    @Test
    @DisplayName("update should return 400 when source and target are the same")
    void update_shouldReturn400WhenSourceAndTargetAreSame() throws Exception {
        when(transferService.update(anyLong(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Source and target accounts must be different"));

        mockMvc.perform(put("/api/transfers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceAccountId\":1,\"targetAccountId\":1,\"amount\":100.00,\"date\":\"2026-07-09\"}"))
                .andExpect(status().isBadRequest());

        verify(transferService).update(anyLong(), any());
    }

    @Test
    @DisplayName("delete should return 204 when successful")
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/transfers/1"))
                .andExpect(status().isNoContent());

        verify(transferService).delete(1L);
    }

    @Test
    @DisplayName("delete should return 404 when transfer not found")
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found"))
                .when(transferService).delete(anyLong());

        mockMvc.perform(delete("/api/transfers/99"))
                .andExpect(status().isNotFound());

        verify(transferService).delete(99L);
    }
}
