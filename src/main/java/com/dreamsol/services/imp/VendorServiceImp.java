package com.dreamsol.services.imp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.dreamsol.exceptions.*;
import com.dreamsol.dto.ProductDto;
import com.dreamsol.dto.VendorDto;
import com.dreamsol.dto.VendorResponse;
import com.dreamsol.dto.VendorTypeDto;
import com.dreamsol.entities.Product;
import com.dreamsol.entities.Vendor;
import com.dreamsol.entities.VendorType;
import com.dreamsol.repositories.VendorRepo;
import com.dreamsol.repositories.VendorTypeRepo;
import com.dreamsol.services.VendorService;

import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VendorServiceImp implements VendorService {
	@Autowired
	private VendorRepo vendorRepo;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private VendorTypeRepo vendorTypeRepo;


	@Override
	public VendorDto addVendor(VendorDto vendorDto,String path,MultipartFile file) {
		Vendor vendor = this.dtoToVendor(vendorDto);
		Vendor savedVendor = this.vendorRepo.save(vendor);
		String fileName=file.getOriginalFilename();
		String fileNameExtension=fileName.substring(fileName.lastIndexOf('.'));
		String randomID=UUID.randomUUID().toString();
		String newFileName=randomID+fileNameExtension;
		String newFilePath=path+newFileName;
		File file2=new File(path);
		if(!file2.exists())  
		{
			file2.mkdir();
		}
		try
		{
			Files.copy(file.getInputStream(),Paths.get(newFilePath));
			vendor.setProfileImage(newFileName);
			vendorRepo.save(vendor);
		}catch(IOException e) {
			e.printStackTrace();
		}
		return this.vendorToDto(savedVendor);
	}

	@Override
	@Transactional
	public VendorDto updateVendor(VendorDto vendorDto, Integer vendorId) {
		Vendor vendor = this.vendorRepo.findById(vendorId)
				.orElseThrow(() -> new ResourceNotFoundException("Vendor", "Id", vendorId));
		vendor.setName(vendorDto.getName());
		vendor.setMob(vendorDto.getMob());
		vendor.setEmail(vendorDto.getEmail());
		vendor.setBrief(vendorDto.getBrief());
		int vendortype_id = vendor.getVendorType().getId();
		VendorType vendorType = this.vendorTypeRepo.findById(vendortype_id)
				.orElseThrow(() -> new ResourceNotFoundException("VedorType", "Id", vendortype_id));
		this.vendorTypeRepo.delete(vendorType);
		vendor.setVendorType(this.dtoToVendorType(vendorDto.getVendorTypeDto()));
		Vendor updatedVendor = this.vendorRepo.save(vendor);
		VendorDto vendorDto1 = this.vendorToDto(updatedVendor);

		return vendorDto1;
	}

	@Override
	public VendorDto getVendorById(Integer vendorId) {
		Vendor vendor = this.vendorRepo.findById(vendorId)
				.orElseThrow(() -> new ResourceNotFoundException("Vendor", "Id", vendorId));
		return this.vendorToDto(vendor);
	}

	@Override
	public VendorResponse getAllVendor(Integer pageNumber, Integer pageSize, String sortBy, String sortDir,
			String keyword) {
		Sort sort = null;
		if (sortDir.equalsIgnoreCase("asc")) {
			sort = Sort.by(sortBy).ascending();
		} else {
			sort = Sort.by(sortBy).descending();
		}

		Pageable p = PageRequest.of(pageNumber, pageSize, sort);

		Page<Vendor> pageVendor;
		if (keyword != null && !keyword.isEmpty()) {
			pageVendor = this.vendorRepo.findByNameContainingIgnoreCase("%" + keyword + "%", p);
		} else {
			pageVendor = this.vendorRepo.findAll(p);
		}

		// Extracting the list of vendors from the retrieved page
		List<Vendor> vendors = pageVendor.getContent();

		// Converting the list of Vendor entities to a list of VendorDto objects
		List<VendorDto> vendorDtos = vendors.stream().map(vendor -> this.vendorToDto(vendor))
				.collect(Collectors.toList());

		// Creating a VendorResponse object and setting its properties
		VendorResponse vendorResponse = new VendorResponse();
		vendorResponse.setContent(vendorDtos);
		vendorResponse.setPageNumber(pageVendor.getNumber());
		vendorResponse.setPageSize(pageVendor.getSize());
		vendorResponse.setTotalElements(pageVendor.getTotalElements());
		vendorResponse.setTotalPages(pageVendor.getTotalPages());
		vendorResponse.setLastPage(pageVendor.isLast());

		// Returning the VendorResponse object
		return vendorResponse;
	}

	@Override
	public void deleteVendor(Integer vendorId) {
		Vendor vendor = this.vendorRepo.findById(vendorId)
				.orElseThrow(() -> new ResourceNotFoundException("Vendor", "Id", vendorId));
		this.vendorRepo.delete(vendor);
	}

	public Vendor dtoToVendor(VendorDto vendorDto) {
		Vendor vendor = this.modelMapper.map(vendorDto, Vendor.class);
		vendor.setVendorType(this.dtoToVendorType(vendorDto.getVendorTypeDto()));
		vendor.setProducts(this.dtoToProduct(vendorDto.getProductDto()));
		return vendor;
	}

	public VendorDto vendorToDto(Vendor vendor) {
		VendorDto vendorDto = this.modelMapper.map(vendor, VendorDto.class);
		vendorDto.setVendorTypeDto(this.VendorTypeToDto(vendor.getVendorType()));
		vendorDto.setProductDto(this.productsToDtos(vendor.getProducts()));
		return vendorDto;
	}

	public VendorType dtoToVendorType(VendorTypeDto vendorTypeDto) {
		VendorType vendorType = this.modelMapper.map(vendorTypeDto, VendorType.class);
		return vendorType;
	}

	public VendorTypeDto VendorTypeToDto(VendorType vendorType) {
		VendorTypeDto vendorTypeDto = this.modelMapper.map(vendorType, VendorTypeDto.class);
		return vendorTypeDto;
	}

	public Set<Product> dtoToProduct(Set<ProductDto> productDtos) 
	{
		Set<Product> products=productDtos.stream().map((productDto)->{
			 Product prod=this.modelMapper.map(productDto, Product.class);
		      return prod;
		}).collect(Collectors.toSet());
		return products;
	}

	public Set<ProductDto> productsToDtos(Set<Product> products) 
	{
		Set<ProductDto> productDtos=products.stream().map((product)->{
			ProductDto prodDto=this.modelMapper.map(product, ProductDto.class);
			return prodDto;
		}).collect(Collectors.toSet());
		return productDtos;
	}
}