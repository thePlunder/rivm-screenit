package nl.rivm.screenit.main.web.gebruiker.screening.mamma.be.werklijst;

/*-
 * ========================LICENSE_START=================================
 * screenit-web
 * %%
 * Copyright (C) 2012 - 2022 Facilitaire Samenwerking Bevolkingsonderzoek
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.rivm.screenit.Constants;
import nl.rivm.screenit.main.model.mamma.beoordeling.MammaBeWerklijstZoekObject;
import nl.rivm.screenit.main.service.mamma.MammaBeoordelingService;
import nl.rivm.screenit.main.web.ScreenitSession;
import nl.rivm.screenit.main.web.component.ComponentHelper;
import nl.rivm.screenit.main.web.component.dropdown.ScreenitListMultipleChoice;
import nl.rivm.screenit.main.web.component.form.PostcodeField;
import nl.rivm.screenit.main.web.component.table.ClientColumn;
import nl.rivm.screenit.main.web.component.table.EnumPropertyColumn;
import nl.rivm.screenit.main.web.component.table.GeboortedatumColumn;
import nl.rivm.screenit.main.web.component.table.ScreenitDataTable;
import nl.rivm.screenit.main.web.gebruiker.screening.mamma.be.AbstractMammaBePage;
import nl.rivm.screenit.main.web.gebruiker.screening.mamma.be.MammaBeTabelCounterPanel;
import nl.rivm.screenit.main.web.security.SecurityConstraint;
import nl.rivm.screenit.model.BeoordelingsEenheid;
import nl.rivm.screenit.model.OrganisatieType;
import nl.rivm.screenit.model.enums.Actie;
import nl.rivm.screenit.model.enums.Bevolkingsonderzoek;
import nl.rivm.screenit.model.enums.LogGebeurtenis;
import nl.rivm.screenit.model.enums.Recht;
import nl.rivm.screenit.model.mamma.MammaBeoordeling;
import nl.rivm.screenit.model.mamma.MammaLezing;
import nl.rivm.screenit.model.mamma.MammaScreeningsEenheid;
import nl.rivm.screenit.model.mamma.enums.MammaBeoordelingStatus;
import nl.rivm.screenit.service.LogService;
import nl.rivm.screenit.service.mamma.MammaBaseBeoordelingService;
import nl.topicuszorg.wicket.component.link.IndicatingAjaxSubmitLink;
import nl.topicuszorg.wicket.hibernate.SimpleListHibernateModel;
import nl.topicuszorg.wicket.hibernate.util.ModelUtil;
import nl.topicuszorg.wicket.input.validator.BSNValidator;
import nl.topicuszorg.wicket.search.column.DateTimePropertyColumn;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wicketstuff.shiro.ShiroConstraint;

@SecurityConstraint(
	actie = Actie.INZIEN,
	checkScope = true,
	constraint = ShiroConstraint.HasPermission,
	recht = { Recht.GEBRUIKER_SCREENING_MAMMA_BEOORDELING_WERKLIJST },
	organisatieTypeScopes = { OrganisatieType.BEOORDELINGSEENHEID },
	bevolkingsonderzoekScopes = { Bevolkingsonderzoek.MAMMA })
public abstract class AbstractMammaBeWerklijstPage extends AbstractMammaBePage
{

	@SpringBean
	private MammaBeoordelingService beoordelingService;

	@SpringBean
	private MammaBaseBeoordelingService baseBeoordelingService;

	@SpringBean
	private LogService logService;

	private Form<MammaBeWerklijstZoekObject> zoekForm;

	private IModel<MammaBeWerklijstZoekObject> zoekObjectModel = new CompoundPropertyModel<>(new MammaBeWerklijstZoekObject());

	private IModel<List<MammaScreeningsEenheid>> screeningsEenhedenModel = new SimpleListHibernateModel<>(new ArrayList<>());

	private ScreenitListMultipleChoice<MammaScreeningsEenheid> screeningsEenhedenSelector;

	private boolean emailHandtekeningVerstuurd = false;

	private final WebMarkupContainer refreshContainer;

	private Component lezingBevestigenBtn;

	public AbstractMammaBeWerklijstPage()
	{
		List<MammaScreeningsEenheid> screeningsEenheden = getMogelijkeScreeningsEenheden();

		if (CollectionUtils.isNotEmpty(screeningsEenheden))
		{
			screeningsEenhedenModel = ModelUtil.listRModel(screeningsEenheden);
		}
		if (ScreenitSession.get().isZoekObjectGezetForComponent(getPageClass()))
		{
			zoekObjectModel = (IModel<MammaBeWerklijstZoekObject>) ScreenitSession.get().getZoekObject(getPageClass());
		}
		else
		{
			resetZoekObject();
		}

		final boolean heeftHandtekekingVoorScreenen = ScreenitSession.get().getLoggedInInstellingGebruiker().getMedewerker().getHandtekening() != null;
		MammaOnderzoekDataProvider onderzoekDataProvider = new MammaOnderzoekDataProvider("onderzoek.creatieDatum", zoekObjectModel);

		refreshContainer = new WebMarkupContainer("refreshContainer");
		refreshContainer.setOutputMarkupId(Boolean.TRUE);
		add(refreshContainer);

		List<IColumn<MammaBeoordeling, String>> columns = new ArrayList<>();
		columns.add(new DateTimePropertyColumn<>(Model.of("Onderzoeksdatum"), "onderzoek.creatieDatum", Constants.getDateTimeSecondsFormat()));
		columns.add(new ClientColumn<MammaBeoordeling>("persoon.achternaam", "onderzoek.afspraak.uitnodiging.screeningRonde.dossier.client"));
		columns.add(new GeboortedatumColumn<>("persoon.geboortedatum", "onderzoek.afspraak.uitnodiging.screeningRonde.dossier.client.persoon"));
		columns.add(new PropertyColumn<>(Model.of("BSN"), "persoon.bsn", "onderzoek.afspraak.uitnodiging.screeningRonde.dossier.client.persoon.bsn"));
		columns.add(new PropertyColumn<>(Model.of("SE"), "se.naam", "onderzoek.screeningsEenheid.naam"));
		columns.add(new EnumPropertyColumn<>(Model.of("Status"), "beoordeling.status", "status"));

		refreshContainer.add(new ScreenitDataTable<MammaBeoordeling, String>("resultaten", columns, onderzoekDataProvider, 10, Model.of("beoordeling(en)"))
		{
			@Override
			protected String getCssClass(int index, IModel<MammaBeoordeling> rowModel)
			{
				MammaBeoordeling beoordeling = rowModel.getObject();
				MammaLezing beoordeeldeLezing = null;
				String cssClass = "";
				if (MammaBeoordelingStatus.EERSTE_LEZING_OPGESLAGEN.equals(beoordeling.getStatus()))
				{
					beoordeeldeLezing = beoordeling.getEersteLezing();
				}
				else if (MammaBeoordelingStatus.TWEEDE_LEZING_OPGESLAGEN.equals(beoordeling.getStatus()))
				{
					beoordeeldeLezing = beoordeling.getTweedeLezing();
				}
				if (beoordeeldeLezing != null)
				{
					cssClass += baseBeoordelingService.isLezingVerwijzen(beoordeeldeLezing) ? " verwijzend " : "";
				}
				return super.getCssClass(index, rowModel) + cssClass;
			}

			@Override
			public void onClick(AjaxRequestTarget target, IModel<MammaBeoordeling> model)
			{
				if (heeftHandtekekingVoorScreenen)
				{
					openBeoordelingScherm(target, model, zoekForm.getModel(), onderzoekDataProvider.getSort());
				}
				else
				{
					if (!emailHandtekeningVerstuurd)
					{
						beoordelingService.radioloogHeeftGeenHandtekening(ScreenitSession.get().getLoggedInInstellingGebruiker().getMedewerker());
						emailHandtekeningVerstuurd = true;
					}
					error(getString("error.heeft.geen.handtekening"));
				}
			}

			@Override
			public Panel getCustomPanel(String id)
			{
				IModel<Integer> beoordeeldModel = new IModel<Integer>()
				{
					@Override
					public Integer getObject()
					{
						return beoordelingService.getAantalBeoordeeld(zoekForm.getModel().getObject());
					}
				};

				IModel<Integer> teBeoordelenModel = new IModel<Integer>()
				{
					@Override
					public Integer getObject()
					{
						return (int) getItemCount() - beoordeeldModel.getObject();
					}
				};

				return new MammaBeTabelCounterPanel(id, teBeoordelenModel, beoordeeldModel);
			}
		});

		zoekForm = new Form<>("form", zoekObjectModel);

		FormComponent<String> bsnField = ComponentHelper.addTextField(zoekForm, "bsn", false, 9, false);
		bsnField.add(new BSNValidator());

		ComponentHelper.addTextField(zoekForm, "geboortedatum", false, -1, Date.class, false)
			.add(new AjaxFormComponentUpdatingBehavior("change")
			{
				@Override
				protected void onUpdate(AjaxRequestTarget target)
				{
					target.add(getComponent());
				}
			});
		zoekForm.add(new PostcodeField("postcode"));
		zoekForm.add(new TextField<>("huisnummer", Integer.class));

		screeningsEenhedenSelector = createScreeningsEenhedenSelector();
		zoekForm.add(screeningsEenhedenSelector);

		List<MammaBeoordelingStatus> beoordelingStatussen = getBeschikbarePaginaStatussen();
		ScreenitListMultipleChoice<MammaBeoordelingStatus> onderzoekStatusSelector = new ScreenitListMultipleChoice<>(
			"beoordelingStatussen",
			new PropertyModel<List<MammaBeoordelingStatus>>(zoekObjectModel, "beoordelingStatussen"), beoordelingStatussen,
			new EnumChoiceRenderer<>());
		zoekForm.add(onderzoekStatusSelector);

		IndicatingAjaxSubmitLink zoekenButton = new IndicatingAjaxSubmitLink("zoeken", zoekForm)
		{

			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				super.onSubmit(target);
				if (CollectionUtils.isEmpty(zoekObjectModel.getObject().getBeoordelingStatussen()))
				{
					zoekObjectModel.getObject().setBeoordelingStatussen(getBeschikbarePaginaStatussen());
				}
				if (CollectionUtils.isEmpty(zoekObjectModel.getObject().getScreeningsEenheden()))
				{
					resetSeKeuzelijst();
				}
				ScreenitSession.get().setZoekObject(getPageClass(), zoekForm.getModel());
				target.add(refreshContainer, zoekForm);
				logFilter();
			}
		};
		zoekForm.setDefaultButton(zoekenButton);
		zoekForm.add(zoekenButton);
		add(zoekForm);

		lezingBevestigenBtn = new IndicatingAjaxLink<T>("lezingenBevestigen", null)
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				beoordelingService.bevestig1eEn2eLezingen(ScreenitSession.get().getLoggedInInstellingGebruiker());
				resetZoekObject();
				target.add(refreshContainer, zoekForm);
			}

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
			{
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners()
					.add(new AjaxCallListener()
						.onBefore("$('#" + getMarkupId() + "').prop('disabled',false);")
						.onComplete("$('#" + getMarkupId() + "').prop('disabled',true);"));
			}
		}
			.setOutputMarkupId(true)
			.setOutputMarkupPlaceholderTag(true)
			.setVisible(bevestigenButtonVisible())
			.setEnabled(bevestigenButtonEnabled());
		add(lezingBevestigenBtn);
		logFilter();
	}

	private void resetZoekObject()
	{
		MammaBeWerklijstZoekObject zoekObject = zoekObjectModel.getObject();
		zoekObject.setInstellingGebruiker(ScreenitSession.get().getLoggedInInstellingGebruiker());
		zoekObject.setBeoordelingStatussen(getDefaultStatussen());
		zoekObject.setScreeningsEenheden(screeningsEenhedenModel.getObject());
		zoekObject.setBeoordelingsEenheid((BeoordelingsEenheid) ScreenitSession.get().getInstelling());
		zoekObject.setGeboortedatum(null);
		zoekObject.setBsn(null);
		zoekObject.setPostcode(null);
		zoekObject.setHuisnummer(null);
	}

	@Override
	protected void onCloseLogoutConfirmationDialog(AjaxRequestTarget target)
	{
		target.add(lezingBevestigenBtn
			.setVisible(bevestigenButtonVisible())
			.setEnabled(bevestigenButtonEnabled()));
		target.add(refreshContainer, zoekForm);
	}

	private ScreenitListMultipleChoice<MammaScreeningsEenheid> createScreeningsEenhedenSelector()
	{
		return new ScreenitListMultipleChoice<>(
			"screeningsEenheden",
			new PropertyModel<List<MammaScreeningsEenheid>>(zoekObjectModel, "screeningsEenheden"), screeningsEenhedenModel,
			new ChoiceRenderer<>("naam"));
	}

	private List<MammaScreeningsEenheid> getMogelijkeScreeningsEenheden()
	{
		return beoordelingService.zoekScreeningsEenhedenMetBeWerklijstBeoordeling(ScreenitSession.get().getLoggedInInstellingGebruiker(), getBeschikbarePaginaStatussen());
	}

	private void resetSeKeuzelijst()
	{
		MammaBeWerklijstZoekObject zoekObject = zoekObjectModel.getObject();
		List<MammaScreeningsEenheid> mogelijkeSEs = getMogelijkeScreeningsEenheden();
		screeningsEenhedenSelector.setChoices(ModelUtil.listRModel(mogelijkeSEs));
		zoekObject.setScreeningsEenheden(mogelijkeSEs);
	}

	private void logFilter()
	{
		String filter = "Filter: ";
		MammaBeWerklijstZoekObject zoekObject = zoekObjectModel.getObject();
		if (zoekObject.getGeboortedatum() != null)
		{
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
			filter += "geboortedatum: " + formatter.format(zoekObject.getGeboortedatum()) + ", ";
		}
		if (StringUtils.isNotBlank(zoekObject.getBsn()))
		{
			filter += "BSN: " + zoekObject.getBsn() + ", ";
		}
		if (zoekObject.getHuisnummer() != null)
		{
			filter += "huisnummer: " + zoekObject.getHuisnummer() + ", ";
		}
		if (StringUtils.isNotBlank(zoekObject.getPostcode()))
		{
			filter += "postcode: " + zoekObject.getPostcode() + ", ";
		}
		if (zoekObject.getScreeningsEenheden() != null && !zoekObject.getScreeningsEenheden().isEmpty())
		{
			List<MammaScreeningsEenheid> ses = zoekObject.getScreeningsEenheden();
			filter += "se: [";
			for (int i = 0; i < ses.size(); i++)
			{
				filter += ses.get(i).getNaam();
				if (i < ses.size() - 1)
				{
					filter += ", ";
				}
			}
			filter += "]";
		}

		List<MammaBeoordelingStatus> statussen = zoekObject.getBeoordelingStatussen();
		filter += "statussen: [";
		for (int i = 0; i < statussen.size(); i++)
		{
			filter += statussen.get(i).getNaam();
			if (i < statussen.size() - 1)
			{
				filter += ", ";
			}
		}
		filter += "]";
		logService.logGebeurtenis(LogGebeurtenis.MAMMA_WERKLIJST_INGEZIEN, ScreenitSession.get().getLoggedInInstellingGebruiker(), filter, Bevolkingsonderzoek.MAMMA);
	}

	public boolean bevestigenButtonVisible()
	{
		return false;
	}

	public boolean bevestigenButtonEnabled()
	{
		return false;
	}

	public abstract void openBeoordelingScherm(AjaxRequestTarget target, IModel<MammaBeoordeling> model, IModel<MammaBeWerklijstZoekObject> zoekObject,
		SortParam<String> sortParam);

	public abstract List<MammaBeoordelingStatus> getDefaultStatussen();

	public abstract List<MammaBeoordelingStatus> getBeschikbarePaginaStatussen();

	@Override
	protected void onDetach()
	{
		super.onDetach();
		ModelUtil.nullSafeDetach(zoekObjectModel);
		ModelUtil.nullSafeDetach(screeningsEenhedenModel);
	}
}
