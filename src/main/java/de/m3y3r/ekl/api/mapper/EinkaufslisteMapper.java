package de.m3y3r.ekl.api.mapper;

import java.util.List;

import javax.inject.Inject;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import de.m3y3r.ekl.api.model.ShoppingListGet;
import de.m3y3r.ekl.model.Einkaufsliste;
import de.m3y3r.ekl.model.IdMapping;

@Mapper(componentModel="cdi", imports=IdMapping.class)
abstract public class EinkaufslisteMapper {

	@Inject
	IdMapper idMapper;

	@Mappings({
		@Mapping(target = "id", expression = "java( idMapper.idtoUuid( ekl.getId(), IdMapping.ON_EKL) )"),
		@Mapping(source = "name", target = "name")
	})

	public abstract ShoppingListGet map(Einkaufsliste ekl);

	public abstract List<ShoppingListGet> map(List<Einkaufsliste> eklen);
}
