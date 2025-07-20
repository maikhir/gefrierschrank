package com.gefrierschrank.app.controller;

import com.gefrierschrank.app.dto.CreateItemRequest;
import com.gefrierschrank.app.dto.ItemDto;
import com.gefrierschrank.app.entity.ExpiryType;
import com.gefrierschrank.app.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemControllerUnitTest {

    @Mock
    private ItemService itemService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ItemController itemController;

    private ItemDto testItemDto;
    private CreateItemRequest createRequest;

    @BeforeEach
    void setUp() {
        // Setup test ItemDto
        testItemDto = new ItemDto();
        testItemDto.setId(1L);
        testItemDto.setName("Test Item");
        testItemDto.setCategoryName("Fleisch");
        testItemDto.setQuantity(new BigDecimal("1.5"));
        testItemDto.setUnit("kg");
        testItemDto.setExpiryDate(LocalDate.now().plusDays(7));
        testItemDto.setExpiryType(ExpiryType.BEST_BEFORE);

        // Setup CreateItemRequest
        createRequest = new CreateItemRequest();
        createRequest.setName("Test Item");
        createRequest.setCategoryId(1L);
        createRequest.setQuantity(new BigDecimal("1.5"));
        createRequest.setUnit("kg");
        createRequest.setExpiryDate(LocalDate.now().plusDays(7));
        createRequest.setExpiryType(ExpiryType.BEST_BEFORE);

        // Setup authentication mock
        when(authentication.getName()).thenReturn("testuser");
    }

    @Test
    void getAllItems_ShouldReturnItemList() {
        // Given
        List<ItemDto> itemList = Arrays.asList(testItemDto);
        when(itemService.getAllItemsByUser("testuser")).thenReturn(itemList);

        // When
        ResponseEntity<List<ItemDto>> response = itemController.getAllItems(authentication);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getName()).isEqualTo("Test Item");
        verify(itemService).getAllItemsByUser("testuser");
    }

    @Test
    void getItemsPaginated_ShouldReturnPagedItems() {
        // Given
        Page<ItemDto> pagedItems = new PageImpl<>(Arrays.asList(testItemDto));
        when(itemService.getItemsByUserPaginated(eq("testuser"), any(Pageable.class))).thenReturn(pagedItems);

        // When
        ResponseEntity<Page<ItemDto>> response = itemController.getItemsPaginated(0, 20, authentication);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).hasSize(1);
        verify(itemService).getItemsByUserPaginated(eq("testuser"), any(Pageable.class));
    }

    @Test
    void getItemById_ShouldReturnItem() {
        // Given
        when(itemService.getItemById(1L, "testuser")).thenReturn(testItemDto);

        // When
        ResponseEntity<ItemDto> response = itemController.getItemById(1L, authentication);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("Test Item");
        verify(itemService).getItemById(1L, "testuser");
    }

    @Test
    void createItem_ShouldCreateAndReturnItem() {
        // Given
        when(itemService.createItem(createRequest, "testuser")).thenReturn(testItemDto);

        // When
        ResponseEntity<ItemDto> response = itemController.createItem(createRequest, authentication);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getName()).isEqualTo("Test Item");
        verify(itemService).createItem(createRequest, "testuser");
    }

    @Test
    void deleteItem_ShouldDeleteItem() {
        // Given
        doNothing().when(itemService).deleteItem(1L, "testuser");

        // When
        ResponseEntity<Void> response = itemController.deleteItem(1L, authentication);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(itemService).deleteItem(1L, "testuser");
    }

    @Test
    void searchItemsByName_ShouldReturnMatchingItems() {
        // Given
        List<ItemDto> searchResults = Arrays.asList(testItemDto);
        when(itemService.searchItemsByName("Test", "testuser")).thenReturn(searchResults);

        // When
        ResponseEntity<List<ItemDto>> response = itemController.searchItemsByName("Test", authentication);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(itemService).searchItemsByName("Test", "testuser");
    }

    @Test
    void getExpiringSoonItems_ShouldReturnExpiringItems() {
        // Given
        List<ItemDto> expiringItems = Arrays.asList(testItemDto);
        when(itemService.getExpiringSoonItems(7, "testuser")).thenReturn(expiringItems);

        // When
        ResponseEntity<List<ItemDto>> response = itemController.getExpiringSoonItems(7, authentication);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(itemService).getExpiringSoonItems(7, "testuser");
    }

    @Test
    void getExpiredItems_ShouldReturnExpiredItems() {
        // Given
        List<ItemDto> expiredItems = Arrays.asList(testItemDto);
        when(itemService.getExpiredItems("testuser")).thenReturn(expiredItems);

        // When
        ResponseEntity<List<ItemDto>> response = itemController.getExpiredItems(authentication);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(itemService).getExpiredItems("testuser");
    }
}