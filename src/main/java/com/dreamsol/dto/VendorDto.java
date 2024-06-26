package com.dreamsol.dto;

import java.util.Set;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VendorDto {
	@NotEmpty
	@Size(min = 4, message = "Name must be min of 3 characters!!")
	private String name;
	@Min(value=6000000000L,message="Mobile number must start with [6-9] and have 10 digits")
    @Max(value = 9999999999L,message = "Mobile number must start with [6-9]")
	private long mob;
	@Email(message = "Email address is not Valid!!")
	private String email;
	@NotEmpty
	@Size(max = 200, message = "Description has maximum 200 characters!!")
	private String brief;
	private VendorTypeDto vendorTypeDto;
	private Set<ProductDto> productDto;

}
