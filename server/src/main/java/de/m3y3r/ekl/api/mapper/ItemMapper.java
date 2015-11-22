package de.m3y3r.ekl.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import de.m3y3r.ekl.api.model.ItemPost;
import de.m3y3r.ekl.model.Item;

@Mapper(componentModel="cdi")
public interface ItemMapper {

	@Mappings({
		@Mapping(target="name", source="name"),
		@Mapping(target="menge", source="count"),
		@Mapping(target="unit", source="unit")
	})
	Item map(ItemPost item);
}
