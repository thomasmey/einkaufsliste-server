package de.m3y3r.ekl.api.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import de.m3y3r.ekl.api.model.ShoppingListGet;
import de.m3y3r.ekl.model.Einkaufsliste;

@Mapper(componentModel="cdi", uses = {IdMapper.class} )
public interface EinkaufslisteMapper {

	@Mappings({
		@Mapping(source = "id", target = "id"),
		@Mapping(source = "name", target = "name")
	})
	ShoppingListGet map(Einkaufsliste ekl);

	List<ShoppingListGet> map(List<Einkaufsliste> eklen);
}
