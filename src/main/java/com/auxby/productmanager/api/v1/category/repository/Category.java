package com.auxby.productmanager.api.v1.category.repository;

import com.auxby.productmanager.api.v1.category.dto.CategoryDetailDto;
import com.auxby.productmanager.api.v1.category.dto.LocalizationDto;
import com.auxby.productmanager.entity.base.AuxbyBaseEntity;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Data;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;

@Data
@Entity
@Table(name = "category")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class Category extends AuxbyBaseEntity {
    private String icon;
    private String name;
    @Type(type = "jsonb", parameters = @Parameter(name = "LocalizationDto", value = "com.auxby.productmanager.api.v1.category.dto.LocalizationDto"))
    @Column(columnDefinition = "jsonb")
    private List<LocalizationDto> label;
    private String parentName;
    @Column(name = "add_offer_cost")
    private Integer addOfferCost;
    @Column(name = "place_bid_cost")
    private Integer placeBidCost;
    @Column(name = "categoryDetails", columnDefinition = "jsonb")
    @Type(type = "jsonb", parameters = @Parameter(name = "CategoryDetailDto", value = "com.auxby.productmanager.api.v1.category.dto.CategoryDetailDto"))
    private List<CategoryDetailDto> categoryDetails;
    @Transient
    List<Category> subcategories;
}
