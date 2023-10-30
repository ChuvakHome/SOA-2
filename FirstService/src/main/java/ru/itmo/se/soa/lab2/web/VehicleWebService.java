package ru.itmo.se.soa.lab2.web;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import ru.itmo.se.soa.lab2.dto.PatchBodyDTO;
import ru.itmo.se.soa.lab2.dto.PatchBodyDTO.FieldPatch;
import ru.itmo.se.soa.lab2.dto.VehicleDTO;
import ru.itmo.se.soa.lab2.entity.VehicleEntity;
import ru.itmo.se.soa.lab2.model.FuelType;
import ru.itmo.se.soa.lab2.model.Vehicle;
import ru.itmo.se.soa.lab2.parser.Parser;
import ru.itmo.se.soa.lab2.parser.QueryParseException;
import ru.itmo.se.soa.lab2.reflection.EntityUpdate;
import ru.itmo.se.soa.lab2.service.EntityAlreadyExistsException;
import ru.itmo.se.soa.lab2.service.EntityNotExistException;
import ru.itmo.se.soa.lab2.service.Pagination;
import ru.itmo.se.soa.lab2.service.Sort;
import ru.itmo.se.soa.lab2.service.VehicleService;
import ru.itmo.se.soa.lab2.util.EnumUtils;
import ru.itmo.se.soa.lab2.util.ResponseUtils;
import ru.itmo.se.soa.lab2.util.SortUtils;
import ru.itmo.se.soa.lab2.validator.AscDescSortOrderException;
import ru.itmo.se.soa.lab2.validator.SortFieldNotExistException;

@Path("/vehicles")
public class VehicleWebService {
	private static final String DEFAULT_PAGE_NUMBER_VALUE = "0";
	private static final String DEFAULT_PAGE_SIZE_VALUE = "10";
	
	@Inject
	private VehicleService vehicleService;
	
	@GET
	@Path("")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@QueryParam("sort") String[] sortParam,
						@QueryParam("filter") String filterParam,
						@QueryParam("page") @DefaultValue(DEFAULT_PAGE_NUMBER_VALUE) Integer pageParam,
						@QueryParam("page_size") @DefaultValue(DEFAULT_PAGE_SIZE_VALUE) Integer pageSizeParam) {
		Sort[] sort = null;
		
		try {
			if (sortParam != null)
				sort = SortUtils.toSorts(sortParam);
		} catch (SortFieldNotExistException e) {
			return ResponseUtils.errorRespone(Status.BAD_REQUEST, String.format("Field \"%s\" does not exist", e.getFieldName()));
		} catch (AscDescSortOrderException e) {
			return ResponseUtils.errorRespone(Status.BAD_REQUEST, e.getMessage());
		}
		
		String filterExpression = null;
		
		try {
			if (filterParam != null && !filterParam.isBlank()) {
				Parser.validateQuery(filterParam);
				filterExpression = filterParam;
			}
		} catch (QueryParseException e) {
			System.err.println(e.getMessage());
			System.err.println(e.getQuery());
			System.err.println(" ".repeat(e.getErrorOffset()) + "^");
			
			return ResponseUtils.errorRespone(Status.BAD_REQUEST, String.format("Bad filter: %s", e.getMessage()));
		}
		
		Pagination pagination = new Pagination(pageParam.intValue(), pageSizeParam.intValue());
		
		if (pagination.getPageNumber() < 0)
			return ResponseUtils.errorRespone(Status.BAD_REQUEST, "Page number must be non-negative");
		else if (pagination.getPageSize() < 0)
			return ResponseUtils.errorRespone(Status.BAD_REQUEST, "Page size must be non-negative");
			
		return Response.ok(vehicleService.findAll(sort, filterExpression, pagination)).build();
	}
	
	@POST
	@Path("")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response post(VehicleDTO dto) {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        
        Vehicle vehicle = dto.toBean();

        Set<ConstraintViolation<Vehicle>> constraintViolations = validator.validate(vehicle);
		
        if (constraintViolations.isEmpty()) {
        	try {
        		VehicleEntity e = vehicleService.create(vehicle.toEntity());
    			
        		return Response.created(URI.create("vehicles/" + String.valueOf(e.getId()))).build();
    		} catch (EntityAlreadyExistsException e) {
    			throw new RuntimeException(e);
    		}
        }
        else {
//            for (ConstraintViolation<Vehicle> violation : constraintViolations) {
//                System.err.println(violation.getMessage());
//            }
        	
        	return ResponseUtils.errorRespone(Status.BAD_REQUEST, constraintViolations.stream().findFirst().get().getMessage());
        }
	}
	
	@GET
	@Path("{vehicle-id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@PathParam("vehicle-id") Long id) {		
		if (id == null || id <= 0)
			return ResponseUtils.emptyBadRequestResponse();
		
		Optional<VehicleEntity> entityOptional = vehicleService.findById(id);
		
		if (entityOptional.isPresent()) {
			VehicleEntity entity = entityOptional.get();
			
			return Response.ok(entity, MediaType.APPLICATION_JSON).build();
		}
		else
			return ResponseUtils.emptyNotFoundResponse();
	}
	
	@PUT
	@Path("{vehicle-id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response put(@PathParam("vehicle-id") Long id, @Valid VehicleDTO dto) {
		if (id == null || id <= 0)
			return ResponseUtils.emptyBadRequestResponse();
		
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        
        validator.validate(dto);
        
        Vehicle vehicle = dto.toBean();

        Set<ConstraintViolation<Vehicle>> vehicleConstraintViolations = validator.validate(vehicle);
        
        if (vehicleConstraintViolations.isEmpty()) {
        	try {
    			VehicleEntity replaceEntity = vehicle.toEntity();
    			replaceEntity.setId(id);
    			
    			vehicleService.update(replaceEntity);
    			
    			return ResponseUtils.emptyOKResponse();
    		} catch (EntityNotExistException e) {
    			return ResponseUtils.emptyNotFoundResponse();
    		}
        }
        else {
//            for (ConstraintViolation<Vehicle> violation : constraintViolations) {
//                System.err.println(violation.getMessage());
//            }
            
            return ResponseUtils.errorRespone(Status.BAD_REQUEST, vehicleConstraintViolations.stream().findFirst().get().getMessage());
        }
	}
	
	@PATCH
	@Path("{vehicle-id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response patch(@PathParam("vehicle-id") Long id, PatchBodyDTO dto) {
		if (id == null || id <= 0)
			return ResponseUtils.emptyBadRequestResponse();
		
		EntityUpdate<VehicleEntity> entityUpdates = new EntityUpdate<>(VehicleEntity.class);
		
		if (dto.getFields() != null) {
			for (FieldPatch patch: dto.getFields()) {
				try {
					entityUpdates.addUpdate(patch.getFieldName(), patch.getValue());
				} catch (NoSuchFieldException e) {
					return ResponseUtils.errorRespone(Status.BAD_REQUEST, String.format("No field %s", patch.getFieldName()));
				}
			}
			
			try {
				vehicleService.update(id, entityUpdates);
			} catch (EntityNotExistException e) {
				return ResponseUtils.emptyNotFoundResponse();
			}
		}
		
		return ResponseUtils.emptyOKResponse();
	}
	
	@DELETE
	@Path("{vehicle-id}")
	public Response delete(@PathParam("vehicle-id") Long id) {
		if (id == null || id <= 0)
			return ResponseUtils.emptyBadRequestResponse();
		
		try {
			vehicleService.deleteById(id);
			
			return ResponseUtils.emptyOKResponse();
		} catch (EntityNotExistException e) {
			return ResponseUtils.emptyNotFoundResponse();
		}
	}
	
	@DELETE
	@Path("delete-by-engine-power/{engine-power}")
	public Response deleteByEnginePower(@PathParam("engine-power") Integer enginePower) {
		if (enginePower <= 0)
			return ResponseUtils.emptyBadRequestResponse();
		
		try {
			vehicleService.deleteByEnginePower(enginePower);
			
			return ResponseUtils.emptyOKResponse();
		} catch (EntityNotExistException e) {
			return ResponseUtils.emptyNotFoundResponse();
		}
	}
	
	@GET
	@Path("average-number-of-wheels")
	@Produces(MediaType.APPLICATION_JSON)
	public Response averageNumberOfWheels() {
		return Response.ok(vehicleService.getAverageNumberOfWheels()).build();
	}
	
	@GET
	@Path("fuel-type-less-than/{fuel-type}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fuelTypeLessThan(@PathParam("fuel-type") String fuelType) {
		try {
			return fuelTypeLessThanByOrdinal(Integer.parseInt(fuelType));
		} catch (NumberFormatException e) {
			return fuelTypeLessThanByConstant(fuelType);
		}
	}
	
	private Response fuelTypeLessThanByOrdinal(int fuelTypeOrdinal) {
		FuelType fuelType = EnumUtils.toEnum(FuelType.class, fuelTypeOrdinal);
		
		if (fuelType != null) {
			List<VehicleEntity> entities = vehicleService.getVehiclesFuelTypeLessThan(fuelType);
			
			return entities.isEmpty() ? ResponseUtils.emptyNotFoundResponse() : Response.ok(entities, MediaType.APPLICATION_JSON).build();
		}
		else
			return ResponseUtils.emptyBadRequestResponse();
	}
	
	private Response fuelTypeLessThanByConstant(String fuelTypeConstant) {
		FuelType fuelType = EnumUtils.toEnum(FuelType.class, fuelTypeConstant);
		
		if (fuelType != null) {
			List<VehicleEntity> entities = vehicleService.getVehiclesFuelTypeLessThan(fuelType);
			
			return entities.isEmpty() ? ResponseUtils.emptyNotFoundResponse() : Response.ok(entities, MediaType.APPLICATION_JSON).build();
		}
		else
			return ResponseUtils.emptyBadRequestResponse();
	}
}
