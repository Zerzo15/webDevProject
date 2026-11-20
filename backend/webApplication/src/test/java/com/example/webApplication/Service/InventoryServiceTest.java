package com.example.webApplication.Service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.webApplication.Exception.OutOfStockException;
import com.example.webApplication.Exception.ResourceNotFoundException;
import com.example.webApplication.Repository.ProductVariantRepo;

class InventoryServiceTest {

    private ProductVariantRepo repo;
    private InventoryService service;

    @BeforeEach
    void setUp() {
        repo = mock(ProductVariantRepo.class);
        service = new InventoryService(repo);
    }

    // ---- decrease ----

    @Test
    void decreaseSucceedsWhenRepoUpdatesOneRow() {
        when(repo.decrease(7L, 3)).thenReturn(1);

        assertDoesNotThrow(() -> service.decrease(7L, 3));

        verify(repo).decrease(7L, 3);
    }

    @Test
    void decreaseThrowsOutOfStockWhenVariantExistsButStockIsLow() {
        when(repo.decrease(7L, 3)).thenReturn(0);
        when(repo.existsById(7L)).thenReturn(true);

        OutOfStockException ex = assertThrows(OutOfStockException.class, () -> service.decrease(7L, 3));

        assertEquals(7L, ex.getVariantId());
        assertEquals(3, ex.getRequested());
    }

    @Test
    void decreaseThrowsNotFoundWhenVariantDoesNotExist() {
        when(repo.decrease(99L, 1)).thenReturn(0);
        when(repo.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.decrease(99L, 1));
    }

    @Test
    void decreaseRejectsZeroOrNegativeQuantityWithoutTouchingTheDatabase() {
        assertThrows(IllegalArgumentException.class, () -> service.decrease(7L, 0));
        assertThrows(IllegalArgumentException.class, () -> service.decrease(7L, -2));

        verify(repo, never()).decrease(anyLong(), anyInt());
    }

    // ---- increase ----

    @Test
    void increaseSucceedsWhenRepoUpdatesOneRow() {
        when(repo.increase(7L, 2)).thenReturn(1);

        assertDoesNotThrow(() -> service.increase(7L, 2));

        verify(repo).increase(7L, 2);
    }

    @Test
    void increaseThrowsNotFoundWhenVariantDoesNotExist() {
        when(repo.increase(99L, 2)).thenReturn(0);

        assertThrows(ResourceNotFoundException.class, () -> service.increase(99L, 2));
    }

    @Test
    void increaseRejectsZeroOrNegativeQuantityWithoutTouchingTheDatabase() {
        assertThrows(IllegalArgumentException.class, () -> service.increase(7L, 0));
        assertThrows(IllegalArgumentException.class, () -> service.increase(7L, -1));

        verify(repo, never()).increase(anyLong(), anyInt());
    }

    // ---- initStock ----

    @Test
    void initStockSetsStockOfExistingVariant() {
        when(repo.setStock(7L, 20)).thenReturn(1);

        assertDoesNotThrow(() -> service.initStock(7L, 20));

        verify(repo).setStock(7L, 20);
    }

    @Test
    void initStockAcceptsZero() {
        when(repo.setStock(7L, 0)).thenReturn(1);

        assertDoesNotThrow(() -> service.initStock(7L, 0));
    }

    @Test
    void initStockRejectsNegativeQuantityWithoutTouchingTheDatabase() {
        assertThrows(IllegalArgumentException.class, () -> service.initStock(7L, -1));

        verify(repo, never()).setStock(anyLong(), anyInt());
    }

    @Test
    void initStockThrowsNotFoundWhenVariantDoesNotExist() {
        when(repo.setStock(99L, 5)).thenReturn(0);

        assertThrows(ResourceNotFoundException.class, () -> service.initStock(99L, 5));
    }
}
