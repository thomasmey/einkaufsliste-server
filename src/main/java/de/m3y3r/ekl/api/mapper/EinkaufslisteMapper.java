package de.m3y3r.ekl.api.mapper;

import java.util.List;

import javax.enterprise.inject.spi.CDI;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import de.m3y3r.ekl.api.model.ShoppingListGet;
import de.m3y3r.ekl.model.Einkaufsliste;

@Mapper(componentModel="cdi", imports=CDI.class)
public interface EinkaufslisteMapper {

	@Mappings({
		@Mapping(target = "id", expression = "java( CDI.current().select(IdMapper.class).get().idtoUuid( ekl.getId(), \"einkaufsliste\") )"),
		@Mapping(source = "name", target = "name")
	})
	ShoppingListGet map(Einkaufsliste ekl);

	List<ShoppingListGet> map(List<Einkaufsliste> eklen);
}
