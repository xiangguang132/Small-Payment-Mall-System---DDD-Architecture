package cn.bugstack.domain.materialtype.service;

import cn.bugstack.domain.materialtype.model.aggregate.MaterialTypeAggregate;
import cn.bugstack.domain.materialtype.repository.IMaterialTypeRepository;
import cn.bugstack.types.exception.AppException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MaterialTypeServiceTest {

    @Mock
    private IMaterialTypeRepository materialTypeRepository;

    @InjectMocks
    private MaterialTypeService materialTypeService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldAddMaterialTypeWhenCodeIsUnique() {
        MaterialTypeAggregate materialType = type("T001", 1);
        when(materialTypeRepository.queryByTypeCode("T001")).thenReturn(null);
        when(materialTypeRepository.save(materialType)).thenReturn(10L);

        assertEquals(Long.valueOf(10L), materialTypeService.addNewMaterialType(materialType));
    }

    @Test(expected = AppException.class)
    public void shouldRejectDuplicateTypeCode() {
        MaterialTypeAggregate existed = MaterialTypeAggregate.builder().id(2L).typeCode("T001").build();
        when(materialTypeRepository.queryByTypeCode("T001")).thenReturn(existed);

        materialTypeService.addNewMaterialType(type("T001", 1));
    }

    @Test(expected = AppException.class)
    public void shouldRejectDeleteWhenChildCategoryExists() {
        when(materialTypeRepository.queryById(1L)).thenReturn(type("T001", 1));
        when(materialTypeRepository.countByParentId(1L)).thenReturn(1L);

        materialTypeService.deleteMaterialTypeById(1L);
    }

    @Test
    public void shouldUpdateMaterialTypeAndKeepAuditFields() {
        MaterialTypeAggregate current = MaterialTypeAggregate.builder()
                .id(1L)
                .parentId(0L)
                .name("old")
                .typeCode("T001")
                .sort(1)
                .status(1)
                .isDel(0)
                .build();
        MaterialTypeAggregate updated = MaterialTypeAggregate.builder()
                .id(1L)
                .name("new")
                .typeCode("T002")
                .sort(2)
                .status(0)
                .build();

        when(materialTypeRepository.queryById(1L)).thenReturn(current);
        when(materialTypeRepository.queryByTypeCode("T002")).thenReturn(null);

        materialTypeService.updateMaterialTypeById(updated);

        ArgumentCaptor<MaterialTypeAggregate> captor = ArgumentCaptor.forClass(MaterialTypeAggregate.class);
        verify(materialTypeRepository).updateById(captor.capture());
        MaterialTypeAggregate saved = captor.getValue();
        assertEquals(Long.valueOf(1L), saved.getId());
        assertEquals("new", saved.getName());
        assertEquals("T002", saved.getTypeCode());
        assertEquals(Integer.valueOf(0), saved.getStatus());
        assertEquals(Integer.valueOf(0), saved.getIsDel());
    }

    @Test
    public void shouldReturnCurrentWhenEnableStatusIsSame() {
        MaterialTypeAggregate current = type("T001", 1);
        current.setId(1L);
        when(materialTypeRepository.queryById(1L)).thenReturn(current);

        MaterialTypeAggregate result = materialTypeService.enableMaterialTypeById(1L);

        assertEquals(current, result);
        verify(materialTypeRepository, never()).updateById(current);
    }

    private MaterialTypeAggregate type(String code, Integer status) {
        return MaterialTypeAggregate.builder()
                .parentId(0L)
                .name("raw material type")
                .typeCode(code)
                .sort(1)
                .status(status)
                .build();
    }
}
