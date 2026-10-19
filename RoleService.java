package com.university.hostel.service;

import com.university.hostel.dto.RoleDto;
import com.university.hostel.entity.Role;
import com.university.hostel.exception.ResourceNotFoundException;
import com.university.hostel.repository.RoleRepository;
import com.university.hostel.util.PatchMapper;
import com.university.hostel.util.FilterSpecifications;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final PatchMapper patchMapper;

    public RoleService(RoleRepository roleRepository, PatchMapper patchMapper) {
        this.roleRepository = roleRepository;
        this.patchMapper = patchMapper;
    }

    public List<Role> getAllRoles(String roleName) {
        List<Specification<Role>> filters = new ArrayList<>();
        if (roleName != null && !roleName.isBlank()) {
            filters.add(FilterSpecifications.exactIgnoreCase("roleName", roleName));
        }
        return roleRepository.findAll(Specification.allOf(filters));
    }

    public Role getRoleById(Long id) {
        return roleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
    }

    public Role createRole(RoleDto dto) {
        Role role = Role.builder()
                .roleName(dto.getRoleName())
                .permissionSet(dto.getPermissionSet())
                .build();
        return roleRepository.save(role);
    }

    public Role updateRole(Long id, RoleDto dto) {
        Role role = getRoleById(id);
        role.setRoleName(dto.getRoleName());
        role.setPermissionSet(dto.getPermissionSet());
        return roleRepository.save(role);
    }

    /** Partial update: only the fields present in {@code updates} are changed. */
    public Role patchRole(Long id, Map<String, Object> updates) {
        Role existing = getRoleById(id);
        RoleDto snapshot = RoleDto.builder()
                .roleId(existing.getRoleId())
                .roleName(existing.getRoleName())
                .permissionSet(existing.getPermissionSet())
                .build();
        RoleDto merged = patchMapper.merge(snapshot, updates);
        return updateRole(id, merged);
    }

    public void deleteRole(Long id) {
        roleRepository.delete(getRoleById(id));
    }
}
