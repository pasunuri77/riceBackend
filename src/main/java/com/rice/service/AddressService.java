package com.rice.service;

import com.rice.dto.address.AddressRequest;
import com.rice.dto.address.AddressResponse;
import com.rice.entity.Address;
import com.rice.entity.User;
import com.rice.entity.enums.AddressType;
import com.rice.exception.ApiException;
import com.rice.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    public List<AddressResponse> list(User user) {
        return addressRepository.findByUserIdOrderByIsDefaultDesc(user.getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public AddressResponse create(User user, AddressRequest req) {
        if (req.isDefault()) {
            clearExistingDefault(user.getId());
        }
        Address address = Address.builder()
                .user(user)
                .fullName(req.getFullName())
                .mobile(req.getMobile())
                .altMobile(req.getAltMobile())
                .flat(req.getFlat())
                .building(req.getBuilding())
                .street(req.getStreet())
                .area(req.getArea())
                .landmark(req.getLandmark())
                .village(req.getVillage())
                .city(req.getCity())
                .district(req.getDistrict())
                .state(req.getState())
                .country(req.getCountry())
                .pincode(req.getPincode())
                .type(parseType(req.getType()))
                .instructions(req.getInstructions())
                .isDefault(req.isDefault())
                .build();
        return toResponse(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse update(User user, Long id, AddressRequest req) {
        Address address = getOwned(user, id);
        if (req.isDefault() && !address.isDefault()) {
            clearExistingDefault(user.getId());
        }
        address.setFullName(req.getFullName());
        address.setMobile(req.getMobile());
        address.setAltMobile(req.getAltMobile());
        address.setFlat(req.getFlat());
        address.setBuilding(req.getBuilding());
        address.setStreet(req.getStreet());
        address.setArea(req.getArea());
        address.setLandmark(req.getLandmark());
        address.setVillage(req.getVillage());
        address.setCity(req.getCity());
        address.setDistrict(req.getDistrict());
        address.setState(req.getState());
        if (req.getCountry() != null) address.setCountry(req.getCountry());
        address.setPincode(req.getPincode());
        address.setType(parseType(req.getType()));
        address.setInstructions(req.getInstructions());
        address.setDefault(req.isDefault());
        return toResponse(address);
    }

    @Transactional
    public void delete(User user, Long id) {
        Address address = getOwned(user, id);
        addressRepository.delete(address);
    }

    @Transactional
    public List<AddressResponse> setDefault(User user, Long id) {
        getOwned(user, id); // ownership check
        clearExistingDefault(user.getId());
        Address address = getOwned(user, id);
        address.setDefault(true);
        return list(user);
    }

    private void clearExistingDefault(Long userId) {
        addressRepository.findByUserIdOrderByIsDefaultDesc(userId)
                .forEach(a -> a.setDefault(false));
    }

    private Address getOwned(User user, Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Address not found"));
        if (!address.getUser().getId().equals(user.getId())) {
            throw ApiException.notFound("Address not found");
        }
        return address;
    }

    private AddressType parseType(String type) {
        if (type == null) return AddressType.HOME;
        for (AddressType t : AddressType.values()) {
            if (t.name().equalsIgnoreCase(type)) return t;
        }
        return AddressType.OTHER;
    }

    private AddressResponse toResponse(Address a) {
        return AddressResponse.builder()
                .id(a.getId())
                .fullName(a.getFullName())
                .mobile(a.getMobile())
                .altMobile(a.getAltMobile())
                .flat(a.getFlat())
                .building(a.getBuilding())
                .street(a.getStreet())
                .area(a.getArea())
                .landmark(a.getLandmark())
                .village(a.getVillage())
                .city(a.getCity())
                .district(a.getDistrict())
                .state(a.getState())
                .country(a.getCountry())
                .pincode(a.getPincode())
                .type(capitalize(a.getType().name()))
                .instructions(a.getInstructions())
                .isDefault(a.isDefault())
                .build();
    }

    private String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
