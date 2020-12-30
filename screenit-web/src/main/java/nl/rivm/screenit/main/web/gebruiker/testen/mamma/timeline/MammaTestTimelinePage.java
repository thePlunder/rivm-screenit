package nl.rivm.screenit.main.web.gebruiker.testen.mamma.timeline;

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

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import nl.rivm.screenit.main.dao.mamma.MammaScreeningsEenheidDao;
import nl.rivm.screenit.main.model.GebeurtenisBron;
import nl.rivm.screenit.main.model.ScreeningRondeGebeurtenis;
import nl.rivm.screenit.main.model.TypeGebeurtenis;
import nl.rivm.screenit.main.model.testen.TestTimelineModel;
import nl.rivm.screenit.main.model.testen.TestTimelineRonde;
import nl.rivm.screenit.main.service.mamma.MammaTestTimelineService;
import nl.rivm.screenit.main.web.ScreenitSession;
import nl.rivm.screenit.main.web.component.ComponentHelper;
import nl.rivm.screenit.main.web.component.PercentageBigDecimalField;
import nl.rivm.screenit.main.web.component.ScreenitForm;
import nl.rivm.screenit.main.web.component.dropdown.ScreenitDropdown;
import nl.rivm.screenit.main.web.component.form.PostcodeField;
import nl.rivm.screenit.main.web.component.modal.BootstrapDialog;
import nl.rivm.screenit.main.web.component.modal.IDialog;
import nl.rivm.screenit.main.web.gebruiker.clienten.dossier.GebeurtenisComparator;
import nl.rivm.screenit.main.web.gebruiker.testen.TestenBasePage;
import nl.rivm.screenit.main.web.gebruiker.testen.gedeeld.timeline.TestVervolgKeuzeOptie;
import nl.rivm.screenit.main.web.gebruiker.testen.gedeeld.timeline.components.TestVervolgKeuzeKnop;
import nl.rivm.screenit.main.web.gebruiker.testen.gedeeld.timeline.popups.BijzondereClientDatumPopup;
import nl.rivm.screenit.main.web.security.SecurityConstraint;
import nl.rivm.screenit.model.Client;
import nl.rivm.screenit.model.GbaPersoon;
import nl.rivm.screenit.model.Gemeente;
import nl.rivm.screenit.model.enums.Actie;
import nl.rivm.screenit.model.enums.Bevolkingsonderzoek;
import nl.rivm.screenit.model.enums.Recht;
import nl.rivm.screenit.model.mamma.MammaScreeningsEenheid;
import nl.rivm.screenit.model.mamma.enums.MammaDoelgroep;
import nl.rivm.screenit.service.BerichtToBatchService;
import nl.rivm.screenit.service.ClientService;
import nl.rivm.screenit.service.ICurrentDateSupplier;
import nl.rivm.screenit.service.mamma.MammaBaseTestService;
import nl.rivm.screenit.util.TestBsnGenerator;
import nl.topicuszorg.hibernate.spring.dao.HibernateService;
import nl.topicuszorg.patientregistratie.persoonsgegevens.model.Geslacht;
import nl.topicuszorg.wicket.component.link.IndicatingAjaxSubmitLink;
import nl.topicuszorg.wicket.hibernate.util.ModelUtil;
import nl.topicuszorg.wicket.model.DetachableListModel;
import nl.topicuszorg.wicket.model.SortingListModel;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.EnumLabel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.PropertyListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.wicketstuff.datetime.markup.html.basic.DateLabel;
import org.wicketstuff.shiro.ShiroConstraint;
import org.wicketstuff.wiquery.ui.datepicker.DatePicker;

@SecurityConstraint(
	actie = Actie.VERWIJDEREN,
	checkScope = true,
	constraint = ShiroConstraint.HasPermission,
	recht = Recht.TESTEN,
	bevolkingsonderzoekScopes = {
		Bevolkingsonderzoek.MAMMA })
public class MammaTestTimelinePage extends TestenBasePage
{

	@SpringBean
	private MammaTestTimelineService testTimelineService;

	@SpringBean
	private HibernateService hibernateService;

	@SpringBean
	private ClientService clientService;

	@SpringBean
	private ICurrentDateSupplier dateSupplier;

	@SpringBean
	private MammaBaseTestService baseTestService;

	@SpringBean
	private MammaScreeningsEenheidDao screeningsEenheidDao;

	@SpringBean(name = "portaalUrl")
	private String clientportaalUrl;

	@SpringBean
	private BerichtToBatchService berichtToBatchService;

	private IModel<TestTimelineModel> model;

	private IModel<List<Client>> clientModel;

	private IModel<List<TestTimelineRonde>> rondesModel;

	private IModel<List<Gemeente>> gemeentenModel;

	private Form<TestTimelineModel> form;

	private WebMarkupContainer formComponents;

	private DatePicker<Date> geboortedatum;

	private TextField<BigDecimal> deelnamekansField;

	private final BootstrapDialog dialog;

	private IModel<List<FileUpload>> pocfilesUploaded = new ListModel<>();

	private IModel<MammaScreeningsEenheid> screeningsEenheid;

	private IModel<ImportPocOpties> importPocOptiesIModel;

	public MammaTestTimelinePage()
	{
		dialog = new BootstrapDialog("dialog")
		{
			@Override
			public boolean fade()
			{
				return false;
			}
		};
		add(dialog);

		List<Gemeente> gemeenten = hibernateService.getHibernateSession().createCriteria(Gemeente.class).add(Restrictions.isNotNull("screeningOrganisatie"))
			.addOrder(Order.asc("naam")).list();

		gemeentenModel = ModelUtil.listRModel(gemeenten, false);

		TestTimelineModel testTimelineModel = new TestTimelineModel();
		testTimelineModel.setGeslacht(Geslacht.VROUW);
		testTimelineModel.setGeboortedatum(dateSupplier.getDateTime().minusYears(50).toDate());
		testTimelineModel.setDeelnamekans(BigDecimal.valueOf(1));
		testTimelineModel.setDoelgroep(MammaDoelgroep.REGULIER);
		testTimelineModel.setPostcode("1234AA");

		model = new CompoundPropertyModel<>(testTimelineModel);
		TestTimelineModel object = model.getObject();
		object.setBsn(TestBsnGenerator.getValideBsn());

		form = new Form<>("form", model);
		form.setOutputMarkupId(true);
		add(form);

		formComponents = getFormComponentsContainer();
		form.add(formComponents);

		gebeurtenissenContainer = getGebeurtenissenContainer();
		form.add(gebeurtenissenContainer);

		Form<Void> clientResetForm = new ScreenitForm<>("clientResetForm");
		final IModel<String> bsns = new Model<>("");
		clientResetForm.add(new TextArea<>("bsns", bsns).setRequired(true));
		clientResetForm.add(new IndicatingAjaxButton("resetten", clientResetForm)
		{
			@Override
			public void onSubmit(AjaxRequestTarget target)
			{
				String message = baseTestService.clientenResetten(bsns.getObject());
				if (message.contains("Succesvol"))
				{
					info(message);
				}
				else
				{
					error(message);
				}
			}
		});

		add(clientResetForm);

		Form<Void> pocClienten = new ScreenitForm<>("pocClienten");
		pocClienten.add(new FileUploadField("csv", pocfilesUploaded));
		IModel<List<MammaScreeningsEenheid>> listRModel = ModelUtil
			.listRModel(screeningsEenheidDao.getActieveScreeningsEenhedenVoorScreeningOrganisatie(ScreenitSession.get().getScreeningOrganisatie()));
		ScreenitDropdown<MammaScreeningsEenheid> screeningsEenheidDropDown = ComponentHelper.newDropDownChoice("screeningsEenheid", listRModel, new ChoiceRenderer<>("naam"), true);

		screeningsEenheidDropDown.setModel(new PropertyModel<>(this, "screeningsEenheid"));
		pocClienten.add(screeningsEenheidDropDown);

		importPocOptiesIModel = new Model<>();
		ScreenitDropdown<ImportPocOpties> laatsteOnderzoekenKlaarzettenVoorSe = ComponentHelper.newDropDownChoice("laatsteRonde",
			new ListModel<>(Arrays.asList(ImportPocOpties.values())), new EnumChoiceRenderer<>(), true);
		laatsteOnderzoekenKlaarzettenVoorSe.setModel(importPocOptiesIModel);
		pocClienten.add(laatsteOnderzoekenKlaarzettenVoorSe);

		pocClienten.add(new IndicatingAjaxButton("import", pocClienten)
		{
			@Override
			public void onSubmit(AjaxRequestTarget target)
			{
				File file = null;
				int aantal = 0;
				try
				{
					FileUpload upload = pocfilesUploaded.getObject().get(0);
					file = upload.writeToTempFile();
					aantal = testTimelineService.importPocClienten(file, ScreenitSession.get().getLoggedInInstellingGebruiker(), getScreeningsEenheid(),
						importPocOptiesIModel.getObject());
				}
				catch (Exception e)
				{
					error("Er is een fout opgetreden: " + e.getMessage());
				}
				finally
				{
					if (file != null && file.exists())
					{
						file.delete();
					}
				}
				info(aantal + " onderzoeken voor de PoC geimporteerd.");
			}
		});

		add(pocClienten);

		add(new MammaTestBulkDeelnamekansPanel("deelnamekansen"));
	}

	private WebMarkupContainer getFormComponentsContainer()
	{
		WebMarkupContainer container = new WebMarkupContainer("formComponents");
		container.setOutputMarkupId(true);

		bsnField = new TextField<>("bsn");
		bsnField.setRequired(true);
		bsnField.setOutputMarkupId(true);
		container.add(bsnField);

		bsnField.add(new AjaxEventBehavior("change")
		{

			private static final long serialVersionUID = 1L;

			@Override
			protected void onEvent(AjaxRequestTarget target)
			{
				WebMarkupContainer geContainer = getGebeurtenissenContainer();
				gebeurtenissenContainer.replaceWith(geContainer);
				gebeurtenissenContainer = geContainer;
				gebeurtenissenContainer.setVisible(false);
				target.add(gebeurtenissenContainer);
			}
		});

		addClientBsnGenererenButtons(container, model);

		geboortedatum = ComponentHelper.monthYearDatePicker("geboortedatum");
		geboortedatum.setOutputMarkupId(true);
		container.add(geboortedatum);

		deelnamekansField = new PercentageBigDecimalField("deelnamekans");

		container.add(deelnamekansField);

		List<Geslacht> geslachten = new ArrayList<>(Arrays.asList(Geslacht.values()));
		geslachten.remove(Geslacht.NIET_GESPECIFICEERD);
		geslachten.remove(Geslacht.ONBEKEND);

		container.add(new DropDownChoice<>("gemeente", gemeentenModel, new ChoiceRenderer<Gemeente>("naam")));

		container.add(new DropDownChoice<MammaDoelgroep>("doelgroep", Arrays.asList(MammaDoelgroep.values()), new EnumChoiceRenderer<MammaDoelgroep>()).setNullValid(true));

		container.add(new PostcodeField("postcode"));

		IndicatingAjaxSubmitLink clientVindOfMaak = new IndicatingAjaxSubmitLink("clientVindOfMaak")
		{

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				TestTimelineModel timelineModel = model.getObject();
				if ((timelineModel.getDoelgroep() != null) != (timelineModel.getDeelnamekans() != null))
				{
					error("Als de doelgroep ingevuld is moet ook de deelnamekans ingevuld worden en vice versa");
					return;
				}
				List<Client> clienten = testTimelineService.maakOfVindClienten(timelineModel);
				List<String> errors = testTimelineService.validateTestClienten(clienten);
				for (String error : errors)
				{
					MammaTestTimelinePage.this.error(error);
				}
				clientModel = ModelUtil.listModel(clienten);
				MammaTestTimelinePage.this.info("Client(en) zijn gevonden en/of succesvol aangemaakt");

				if (clienten.size() > 0)
				{
					refreshTimelineModel(timelineModel, clienten);
					WebMarkupContainer fCcontainer = getFormComponentsContainer();
					formComponents.replaceWith(fCcontainer);
					formComponents = fCcontainer;
					target.add(formComponents);
				}

				WebMarkupContainer geContainer = getGebeurtenissenContainer();
				gebeurtenissenContainer.replaceWith(geContainer);
				gebeurtenissenContainer = geContainer;
				target.add(gebeurtenissenContainer);
			}
		};
		form.setDefaultButton(clientVindOfMaak);
		container.add(clientVindOfMaak);
		IndicatingAjaxSubmitLink clientWijzigOfMaak = new IndicatingAjaxSubmitLink("clientWijzigOfMaak")
		{

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				TestTimelineModel timelineModel = model.getObject();
				if ((timelineModel.getDoelgroep() != null) != (timelineModel.getDeelnamekans() != null))
				{
					error("Als de doelgroep ingevuld is moet ook de deelnamekans ingevuld worden en vice versa");
					return;
				}
				List<Client> clienten = testTimelineService.maakOfWijzigClienten(timelineModel);
				List<String> errors = testTimelineService.validateTestClienten(clienten);
				for (String error : errors)
				{
					MammaTestTimelinePage.this.error(error);
				}
				clientModel = ModelUtil.listModel(clienten);
				MammaTestTimelinePage.this.info("Client(en) zijn succesvol gewijzigd of aangemaakt");

				if (clienten.size() > 0)
				{
					refreshTimelineModel(timelineModel, clienten);
					WebMarkupContainer fCcontainer = getFormComponentsContainer();
					formComponents.replaceWith(fCcontainer);
					formComponents = fCcontainer;
					target.add(formComponents);
				}

				WebMarkupContainer geContainer = getGebeurtenissenContainer();
				gebeurtenissenContainer.replaceWith(geContainer);
				gebeurtenissenContainer = geContainer;
				target.add(gebeurtenissenContainer);
			}
		};
		container.add(clientWijzigOfMaak);
		return container;
	}

	@Override
	protected WebMarkupContainer getGebeurtenissenContainer()
	{
		WebMarkupContainer container = new WebMarkupContainer("gebeurtenissenContainer");
		container.setOutputMarkupPlaceholderTag(true);

		container.setVisible(clientModel != null);
		if (clientModel != null)
		{
			reloadClienten();
			List<TestTimelineRonde> rondes = testTimelineService.getTimelineRondes(clientModel.getObject().get(0));
			Collections.sort(rondes, (o1, o2) -> o2.getRondeNummer().compareTo(o1.getRondeNummer()));
			rondesModel = new DetachableListModel<>(rondes);

			TestVervolgKeuzeKnop snelkeuzeRonde = new TestVervolgKeuzeKnop("nieuweRonde", clientModel, dialog)
			{

				private static final long serialVersionUID = 1L;

				@Override
				public boolean refreshContainer(AjaxRequestTarget target)
				{
					List<Client> clienten = testTimelineService.maakOfVindClienten(model.getObject());
					List<String> errors = testTimelineService.validateTestClienten(clienten);
					for (String error : errors)
					{
						error(error);
					}
					clientModel = ModelUtil.listModel(clienten);

					WebMarkupContainer geContainer = getGebeurtenissenContainer();
					gebeurtenissenContainer.replaceWith(geContainer);
					gebeurtenissenContainer = geContainer;
					target.add(gebeurtenissenContainer);
					return true;
				}

				@Override
				public List<TestVervolgKeuzeOptie> getOptions()
				{
					List<TestVervolgKeuzeOptie> keuzes = new ArrayList<>();
					if (clientModel.getObject().get(0).getPersoon().getGeslacht() == Geslacht.VROUW)
					{
						keuzes.add(TestVervolgKeuzeOptie.MAMMA_NIEUWE_RONDE_MET_AFSPRAAK_UITNODIGING);
						keuzes.add(TestVervolgKeuzeOptie.MAMMA_NIEUWE_RONDE_MET_OPEN_UITNODIGING);
						if (clientModel.getObject().get(0).getMammaDossier().getLaatsteScreeningRonde() != null)
						{
							keuzes.add(TestVervolgKeuzeOptie.MAMMA_VERZET_TIJD);
						}
					}
					return keuzes;
				}

				@Override
				public boolean isVisible()
				{
					GbaPersoon persoon = clientModel.getObject().get(0).getPersoon();
					boolean isOverleden = persoon.getOverlijdensdatum() != null;
					return !isOverleden;
				}

				@Override
				public String getNameAttribuut()
				{
					return "snelkeuze_ronde";
				}
			};
			container.add(snelkeuzeRonde);

			ListView<TestTimelineRonde> listView = getListView();
			listView.setOutputMarkupId(true);
			container.add(listView);
		}

		addbuttons(container);
		return container;
	}

	private void reloadClienten()
	{
		List<Client> reloadedClienten = new ArrayList<>();
		for (Client client : clientModel.getObject())
		{
			reloadedClienten.add(hibernateService.load(Client.class, client.getId()));
		}
		clientModel = ModelUtil.listModel(reloadedClienten);
	}

	private ListView<TestTimelineRonde> getListView()
	{
		ListView<TestTimelineRonde> listView = new ListView<TestTimelineRonde>("rondes", rondesModel)
		{

			@Override
			protected void populateItem(ListItem<TestTimelineRonde> item)
			{
				item.add(new Label("rondeNummer", item.getModelObject().getRondeNummer()));
				item.add(new TestVervolgKeuzeKnop("snelKeuzeMamma", clientModel, dialog)
				{

					@Override
					public List<TestVervolgKeuzeOptie> getOptions()
					{
						return testTimelineService.getSnelKeuzeOpties(clientModel.getObject().get(0));
					}

					@Override
					public boolean refreshContainer(AjaxRequestTarget target)
					{
						WebMarkupContainer geContainer = getGebeurtenissenContainer();
						gebeurtenissenContainer.replaceWith(geContainer);
						gebeurtenissenContainer = geContainer;
						target.add(gebeurtenissenContainer);
						return true;
					}

					@Override
					public boolean isVisible()
					{
						Client client = clientModel.getObject().get(0);
						TestTimelineRonde timeLineRonde = item.getModelObject();
						return testTimelineService.isSnelkeuzeKnopMammaBeschikbaar(client, timeLineRonde);
					}

					@Override
					public String getNameAttribuut()
					{
						return "snelkeuze_gebeurtenis";
					}
				});
				SortingListModel<ScreeningRondeGebeurtenis> sortingListModel = new SortingListModel<>(
					new PropertyModel<>(item.getModel(), "mammaScreeningRondeDossier.gebeurtenissen"), new GebeurtenisComparator());

				item.add(new PropertyListView<ScreeningRondeGebeurtenis>("gebeurtenissen", sortingListModel)
				{

					private static final long serialVersionUID = 1L;

					@Override
					protected void populateItem(final ListItem<ScreeningRondeGebeurtenis> item)
					{
						item.add(DateLabel.forDatePattern("datum", "dd-MM-yyyy HH:mm:ss"));
						item.add(new EnumLabel<TypeGebeurtenis>("gebeurtenis"));
						item.add(new EnumLabel<GebeurtenisBron>("bron"));
						item.add(new AttributeAppender("class", new Model<String>("badge-not-clickable"), " "));
						item.add(new Label("extraOmschrijving", new IModel<String>()
						{

							private static final long serialVersionUID = 1L;

							@Override
							public String getObject()
							{
								ScreeningRondeGebeurtenis gebeurtenis2 = item.getModelObject();
								String[] extraOmschrijvingen = gebeurtenis2.getExtraOmschrijving();
								String extraOmschrijving = "";
								if (extraOmschrijvingen != null)
								{
									for (String omschrijving : extraOmschrijvingen)
									{
										if (omschrijving != null)
										{
											if (StringUtils.isNotBlank(extraOmschrijving))
											{
												if (extraOmschrijving.trim().endsWith(":"))
												{
													if (!extraOmschrijving.endsWith(":"))
													{
														extraOmschrijving += " ";
													}
												}
												else
												{
													extraOmschrijving += ", ";
												}
											}
											else
											{
												extraOmschrijving = "(";
											}
											extraOmschrijving += getString(omschrijving, null, omschrijving);
										}
									}
								}
								if (StringUtils.isNotBlank(extraOmschrijving))
								{
									extraOmschrijving += ")";
								}
								return extraOmschrijving;
							}
						}));
					}
				});
			}
		};
		return listView;
	}

	private void addbuttons(WebMarkupContainer container)
	{
		container.add(new AjaxButton("bijzondereClientData", form)
		{
			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				dialog.openWith(target, new BijzondereClientDatumPopup(IDialog.CONTENT_ID, clientModel)
				{
					@Override
					public void close(AjaxRequestTarget target)
					{
						dialog.close(target);
						WebMarkupContainer geContainer = getGebeurtenissenContainer();
						gebeurtenissenContainer.replaceWith(geContainer);
						gebeurtenissenContainer = geContainer;
						target.add(gebeurtenissenContainer);
						info("Client aangepast.");
					}
				});
			}

			@Override
			public boolean isVisible()
			{
				return clientModel.getObject().get(0).getPersoon().getOverlijdensdatum() == null;
			}
		});
		container.add(getClientDossierButton(form, model));
		container.add(getClientPortaalButton(form, clientportaalUrl, model));
		final AjaxCheckBox verstuurHl7BerichtenCheckbox = new AjaxCheckBox("verstuurHl7Berichten", verstuurHl7Berichten)
		{
			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{

			}
		};
		container.add(verstuurHl7BerichtenCheckbox);
	}

	private Model<Boolean> verstuurHl7Berichten = Model.of(false);

	public Model<Boolean> getVerstuurHl7Berichten()
	{
		return verstuurHl7Berichten;
	}

	public void setVerstuurHl7Berichten(Model<Boolean> verstuurHl7Berichten)
	{
		this.verstuurHl7Berichten = verstuurHl7Berichten;
	}

	public MammaScreeningsEenheid getScreeningsEenheid()
	{
		return ModelUtil.nullSafeGet(screeningsEenheid);
	}

	public void setScreeningsEenheid(MammaScreeningsEenheid screeningsEenheid)
	{
		this.screeningsEenheid = ModelUtil.sModel(screeningsEenheid);
	}

	@Override
	protected void detachModel()
	{
		super.detachModel();
		ModelUtil.nullSafeDetach(model);
		ModelUtil.nullSafeDetach(gemeentenModel);
		ModelUtil.nullSafeDetach(clientModel);
		ModelUtil.nullSafeDetach(rondesModel);
		ModelUtil.nullSafeDetach(screeningsEenheid);
		ModelUtil.nullSafeDetach(importPocOptiesIModel);
	}

}
