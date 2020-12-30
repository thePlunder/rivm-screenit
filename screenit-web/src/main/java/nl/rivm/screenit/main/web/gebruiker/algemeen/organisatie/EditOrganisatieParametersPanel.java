package nl.rivm.screenit.main.web.gebruiker.algemeen.organisatie;

/*-
 * ========================LICENSE_START=================================
 * screenit-web
 * %%
 * Copyright (C) 2012 - 2020 Facilitaire Samenwerking Bevolkingsonderzoek
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * =========================LICENSE_END==================================
 */

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.rivm.screenit.Constants;
import nl.rivm.screenit.main.util.EnumStringUtil;
import nl.rivm.screenit.model.Instelling;
import nl.rivm.screenit.model.OrganisatieParameter;
import nl.rivm.screenit.model.OrganisatieParameterKey;
import nl.rivm.screenit.model.OrganisatieType;
import nl.rivm.screenit.service.InstellingService;
import nl.rivm.screenit.util.BigDecimalUtil;
import nl.topicuszorg.wicket.hibernate.util.ModelUtil;
import nl.topicuszorg.wicket.input.validator.StringIsNumberValidator;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.EnumLabel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.RangeValidator;
import org.apache.wicket.validation.validator.StringValidator;

public abstract class EditOrganisatieParametersPanel extends GenericPanel<List<OrganisatieParameterKey>>
{

	@SpringBean
	private InstellingService instellingService;

	private IModel<List<OrganisatieParameter>> allParametersModel = ModelUtil.listModel(new ArrayList<>(), false);

	public EditOrganisatieParametersPanel(String id, List<OrganisatieParameterKey> parameterKeys)
	{
		super(id);

		Form<Void> form = new Form<>("form");
		add(form);
		ListView<OrganisatieParameterKey> parameters = new ListView<OrganisatieParameterKey>("parameters", parameterKeys)
		{

			@Override
			protected void populateItem(ListItem<OrganisatieParameterKey> item)
			{
				OrganisatieParameterKey parameterKey = item.getModelObject();
				item.add(new EnumLabel<OrganisatieParameterKey>("param", parameterKey));
				item.add(new EnumLabel<OrganisatieType>("organisatieType", parameterKey.getOrganisatieType()));
				List<Instelling> instellingByOrganisatieTypes = instellingService.getInstellingByOrganisatieTypes(Arrays.asList(parameterKey.getOrganisatieType()));
				item.add(new Label("unit", getString(EnumStringUtil.getPropertyString(parameterKey) + ".unit")));
				item.add(new Label("maxValue", parameterKey.getMaxValue()));
				addOrganisatieLijst(item, parameterKey, instellingByOrganisatieTypes);

			}

			private void addOrganisatieLijst(ListItem<OrganisatieParameterKey> item, OrganisatieParameterKey parameterKey, List<Instelling> instellingByOrganisatieTypes)
			{
				item.add(new ListView<Instelling>("organisaties", ModelUtil.listRModel(instellingByOrganisatieTypes))
				{

					@Override
					protected void populateItem(ListItem<Instelling> item)
					{
						OrganisatieParameter foundParameter = null;
						for (OrganisatieParameter parameter : item.getModelObject().getParameters())
						{
							if (parameter.getKey() == parameterKey)
							{
								foundParameter = parameter;
							}
						}
						if (foundParameter == null)
						{
							foundParameter = new OrganisatieParameter();
							foundParameter.setOrganisatie(item.getModelObject());
							foundParameter.setKey(parameterKey);
						}
						List<OrganisatieParameter> allParams = allParametersModel.getObject();
						allParams.add(foundParameter);
						item.add(new Label("naam", item.getModelObject().getNaam()));
						TextField<String> valueField = new TextField<String>("value",
							new PropertyModel<>(EditOrganisatieParametersPanel.this, "allParameters[" + (allParams.size() - 1) + "].value"));

						Class<?> valueType = parameterKey.getValueType();
						if (valueType.equals(Integer.class))
						{
							addIntegerValidators(parameterKey, valueField);
						}
						else if (valueType.equals(BigDecimal.class))
						{
							addBigDecimalValidator(parameterKey, valueField);
						}
						else if (valueType.equals(String.class))
						{
							valueField.add(StringValidator.maximumLength(255));
						}
						item.add(valueField);
					}

					private void addIntegerValidators(OrganisatieParameterKey parameterKey, TextField<String> valueField)
					{
						valueField.add(new StringIsNumberValidator());
						valueField.add(new RangeValidator<Integer>(0, parameterKey.getMaxValue())
						{
							@Override
							protected Integer getValue(IValidatable<Integer> validatable)
							{
								Object value = validatable.getValue();
								if (value != null)
								{
									return Integer.valueOf(value.toString());
								}
								return null;
							};
						});
						valueField.add(new AttributeAppender("maxlength", Model.of(parameterKey.getMaxValue().toString().length())));
					}

					private void addBigDecimalValidator(OrganisatieParameterKey parameterKey, TextField<String> valueField)
					{
						valueField.add(new RangeValidator<BigDecimal>(BigDecimal.ZERO, BigDecimal.valueOf(parameterKey.getMaxValue()))
						{

							@Override
							public void validate(IValidatable<BigDecimal> validatable)
							{
								Object value = validatable.getValue();
								if (value != null)
								{
									try
									{
										BigDecimalUtil.stringToBigDecimal(value.toString(), Constants.LOCALE_NL);
									}
									catch (Exception e)
									{
										ValidationError error = new ValidationError(this, "bigdecimal");
										validatable.error(error);
									}
								}
								if (validatable.isValid())
								{
									super.validate(validatable);
								}
							}

							@Override
							protected BigDecimal getValue(IValidatable<BigDecimal> validatable)
							{
								Object value = validatable.getValue();
								if (value != null)
								{
									return BigDecimalUtil.stringToBigDecimal(value.toString(), Constants.LOCALE_NL);
								}
								return null;
							};
						});
					}

				});
			}

		};

		form.add(parameters);
		addOpslaanButton(form);
	}

	protected abstract void addOpslaanButton(Form<Void> form);

	public List<OrganisatieParameter> getAllParameters()
	{
		List<OrganisatieParameter> list = ModelUtil.nullSafeGet(allParametersModel);
		list.forEach(op -> op.setParameterNaam(getString(EnumStringUtil.getPropertyString(op.getKey()))));
		return list;
	}

	@Override
	protected void detachModel()
	{
		super.detachModel();
		ModelUtil.nullSafeDetach(allParametersModel);
	}
}