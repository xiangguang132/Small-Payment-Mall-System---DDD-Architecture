package cn.bugstack.domain.material.service;

import cn.bugstack.domain.material.model.aggregate.MaterialAggregate;
import cn.bugstack.domain.material.repository.IMaterialRepository;
import cn.bugstack.domain.materialtype.model.aggregate.MaterialTypeAggregate;
import cn.bugstack.domain.materialtype.repository.IMaterialTypeRepository;
import cn.bugstack.types.exception.AppException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MaterialServiceTest {

    @Mock
    private IMaterialRepository materialRepository;

    @Mock
    private IMaterialTypeRepository materialTypeRepository;

    @InjectMocks
    private MaterialService materialService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldAddMaterialWhenValidationPasses() {
        MaterialAggregate material = material("M001", "steel", 1L, "kg", 1);
        when(materialRepository.queryByMaterialCode("M001")).thenReturn(null);
        when(materialTypeRepository.queryById(1L)).thenReturn(enabledType(1L));
        when(materialRepository.save(material)).thenReturn(1L);

        Long id = materialService.addNewMaterial(material);

        assertEquals(Long.valueOf(1L), id);
        verify(materialRepository).save(material);
    }

    @Test(expected = AppException.class)
    public void shouldRejectDuplicateMaterialCode() {
        MaterialAggregate existed = MaterialAggregate.builder().id(2L).materialCode("M001").build();
        when(materialRepository.queryByMaterialCode("M001")).thenReturn(existed);

        materialService.addNewMaterial(material("M001", "steel", 1L, "kg", 1));
    }

    @Test(expected = AppException.class)
    public void shouldRejectDeleteWhenMaterialMissing() {
        when(materialRepository.queryById(2L)).thenReturn(null);

        materialService.deleteMaterialById(2L);
    }

    @Test(expected = AppException.class)
    public void shouldRejectDisabledMaterial() {
        MaterialAggregate material = MaterialAggregate.builder()
                .id(1L)
                .isDel(0)
                .status(0)
                .build();
        when(materialRepository.queryById(1L)).thenReturn(material);

        materialService.validateMaterialEnabled(1L);
    }

    @Test
    public void shouldUpdateMaterialAndKeepAuditFields() {
        LocalDateTime createTime = LocalDateTime.of(2026, 8, 1, 10, 0);
        MaterialAggregate current = MaterialAggregate.builder()
                .id(1L)
                .materialCode("M001")
                .name("old")
                .typeId(1L)
                .unit("kg")
                .status(1)
                .isDel(0)
                .createTime(createTime)
                .build();
        MaterialAggregate updated = MaterialAggregate.builder()
                .id(1L)
                .materialCode("M002")
                .name("new")
                .typeId(1L)
                .unit("g")
                .status(0)
                .build();

        when(materialRepository.queryById(1L)).thenReturn(current);
        when(materialRepository.queryByMaterialCode("M002")).thenReturn(null);
        when(materialTypeRepository.queryById(1L)).thenReturn(enabledType(1L));

        materialService.updateMaterialById(updated);

        ArgumentCaptor<MaterialAggregate> captor = ArgumentCaptor.forClass(MaterialAggregate.class);
        verify(materialRepository).updateById(captor.capture());
        MaterialAggregate saved = captor.getValue();
        assertEquals(Long.valueOf(1L), saved.getId());
        assertEquals("M002", saved.getMaterialCode());
        assertEquals("new", saved.getName());
        assertEquals(Integer.valueOf(0), saved.getIsDel());
        assertEquals(createTime, saved.getCreateTime());
        assertNotNull(saved.getUpdateTime());
    }

    private MaterialAggregate material(String code, String name, Long typeId, String unit, Integer status) {
        return MaterialAggregate.builder()
                .materialCode(code)
                .name(name)
                .typeId(typeId)
                .unit(unit)
                .status(status)
                .isDel(0)
                .build();
    }

    private MaterialTypeAggregate enabledType(Long id) {
        return MaterialTypeAggregate.builder().id(id).status(1).build();
    }
}
