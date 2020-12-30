package nl.rivm.screenit.main.web.gebruiker.clienten.contact.colon.huisarts;

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

import nl.rivm.screenit.main.service.RondeNummerService;
import nl.rivm.screenit.main.web.ScreenitSession;
import nl.rivm.screenit.main.web.component.modal.BootstrapDialog;
import nl.rivm.screenit.main.web.component.modal.IDialog;
import nl.rivm.screenit.main.web.gebruiker.clienten.contact.colon.ColonHuisartsWijzigenPanel;
import nl.rivm.screenit.model.EnovationHuisarts;
import nl.rivm.screenit.model.OnbekendeHuisarts;
import nl.rivm.screenit.model.colon.ColonScreeningRonde;
import nl.rivm.screenit.model.enums.Actie;
import nl.rivm.screenit.model.enums.Recht;
import nl.rivm.screenit.model.enums.ToegangLevel;
import nl.rivm.screenit.service.colon.AfspraakService;
import nl.rivm.screenit.util.AdresUtil;
import nl.rivm.screenit.util.NaamUtil;
import nl.topicuszorg.wicket.hibernate.util.ModelUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

public class HuisartsPanel extends GenericPanel<ColonScreeningRonde>
{

	private static final long serialVersionUID = 1L;

	private BootstrapDialog dialog;

	private IModel<EnovationHuisarts> zoekModel;

	private ColonHuisartsWijzigenPanel huisartsWijzigenPanel;

	@SpringBean
	private AfspraakService afspraakService;

	@SpringBean
	private RondeNummerService rondeNummerService;

	public HuisartsPanel(String id, IModel<ColonScreeningRonde> colonScreeningRonde, ColonHuisartsWijzigenPanel huisartsWijzigenPanel,
		BootstrapDialog dialog, IModel<Boolean> huisartsBerichtenVerzenden)
	{
		super(id, colonScreeningRonde);
		setZoekModel(ModelUtil.cModel(new EnovationHuisarts()));
		setDialog(dialog);

		boolean magWijzigen = ScreenitSession.get().checkPermission(Recht.GEBRUIKER_WIJZIGEN_HUISARTS, Actie.AANPASSEN);
		boolean magVerwijderen = ScreenitSession.get().checkPermission(Recht.GEBRUIKER_WIJZIGEN_HUISARTS, Actie.VERWIJDEREN);
		boolean magVerzenden = ScreenitSession.get().checkPermission(Recht.GEBRUIKER_CLIENT_SR_HUISARTSBERICHT_OPNIEUW_VERZENDEN, Actie.AANPASSEN, getModelObject());

		setHuisartsWijzigenPanel(huisartsWijzigenPanel);

		AjaxLink<ColonScreeningRonde> wijzigHuisartsBtn = new IndicatingAjaxLink<ColonScreeningRonde>("wijzigHuisarts", getModel())
		{

			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure()
			{
				super.onConfigure();
				ToegangLevel toegangsLevel = ScreenitSession.get().getToegangsLevel(Actie.AANPASSEN, Recht.GEBRUIKER_WIJZIGEN_HUISARTS);
				setVisible(toegangsLevel != null && ToegangLevel.REGIO.getNiveau() <= toegangsLevel.getNiveau());
			}

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				ColonScreeningRonde huidigeRonde = getModelObject();
				ColonScreeningRonde vorigeRonde = rondeNummerService.getVorigeRonde(huidigeRonde);
				if (vorigeRonde != null && vorigeRonde.getColonHuisarts() != null && huidigeRonde.getColonHuisarts() == null && huidigeRonde.getOnbekendeHuisarts() == null)
				{
					getDialog().openWith(target, new HuisartsVorigeRondeDialogPanel(IDialog.CONTENT_ID, getModel(), ModelUtil.sModel(vorigeRonde.getColonHuisarts()),
						getZoekModel(), getDialog(), getHuisartsWijzigenPanel())
					{

						private static final long serialVersionUID = 1L;

						@Override
						protected void close(AjaxRequestTarget target)
						{
							getDialog().close(target);
						}
					});
				}
				else if (vorigeRonde != null && vorigeRonde.getOnbekendeHuisarts() != null)
				{
					getDialog().openWith(target,
						new OnbekendeHuisartsVorigeRondeDialogPanel(IDialog.CONTENT_ID, getModel(), ModelUtil.cModel(vorigeRonde.getOnbekendeHuisarts()),
							getHuisartsWijzigenPanel(), getDialog())
						{

							private static final long serialVersionUID = 1L;

							@Override
							protected void close(AjaxRequestTarget target)
							{
								getDialog().close(target);
							}
						});
				}
				else
				{
					getDialog().openWith(target,
						new HuisartsZoekenDialogPanel(IDialog.CONTENT_ID, getModel(), getZoekModel(), getDialog(), false, getHuisartsWijzigenPanel())
						{

							private static final long serialVersionUID = 1L;

							@Override
							protected void close(AjaxRequestTarget target)
							{
								getDialog().close(target);
							}
						});
				}
			}
		};
		wijzigHuisartsBtn.setVisible(magWijzigen);
		add(wijzigHuisartsBtn);

		ColonScreeningRonde laatsteScreeningronde = getModelObject();
		boolean vorigeBerichtenBeschikbaar = laatsteScreeningronde.getLaatsteAfspraak() != null;
		EnovationHuisarts enovationHuisarts = laatsteScreeningronde.getColonHuisarts();
		OnbekendeHuisarts onbekendeHuisarts = laatsteScreeningronde.getOnbekendeHuisarts();

		boolean verzendHaBerichtenVisible = magVerzenden && vorigeBerichtenBeschikbaar && (enovationHuisarts != null || onbekendeHuisarts != null);
		final WebMarkupContainer verzendHaBerichtenOpnieuwContainer = new WebMarkupContainer("verzendHaBerichtenOpnieuwContainer");
		verzendHaBerichtenOpnieuwContainer.setVisible(verzendHaBerichtenVisible);
		verzendHaBerichtenOpnieuwContainer.setOutputMarkupId(true);
		add(verzendHaBerichtenOpnieuwContainer);

		huisartsBerichtenVerzenden.setObject(verzendHaBerichtenVisible);
		CheckBox verzendHaBerichtenOpnieuw = new CheckBox("verzendHaBerichtenOpnieuw", huisartsBerichtenVerzenden);
		verzendHaBerichtenOpnieuwContainer.add(verzendHaBerichtenOpnieuw);

		AjaxLink huisartsVerwijderenBtn = new AjaxLink<Void>("verwijderHuisarts")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				HuisartsPanel.this.getModelObject().setOnbekendeHuisarts(null);
				HuisartsPanel.this.getModelObject().setColonHuisarts(null);
				HuisartsPanel.this.get("huisartsNaam").setDefaultModelObject("");
				HuisartsPanel.this.get("praktijkAdres").setDefaultModelObject("");
				HuisartsPanel.this.get("praktijkNaam").setDefaultModelObject("");
				HuisartsPanel.this.get("huisartsAgb").setDefaultModelObject("");
				HuisartsPanel.this.get("weergaveNaam").setDefaultModelObject("");
				HuisartsPanel.this.get("klantnummer").setDefaultModelObject("");
				HuisartsPanel.this.get("ediadres").setDefaultModelObject("");
				HuisartsPanel.this.get("communicatieadres").setDefaultModelObject("");
				verzendHaBerichtenOpnieuwContainer.setVisible(false);
				target.add(verzendHaBerichtenOpnieuwContainer);
				target.add(HuisartsPanel.this);
			}
		};
		add(huisartsVerwijderenBtn);
		huisartsVerwijderenBtn.setVisible(magVerwijderen && (enovationHuisarts != null || onbekendeHuisarts != null));

		if (enovationHuisarts != null)
		{
			add(new Label("huisartsNaam", NaamUtil.getNaamHuisarts(enovationHuisarts)));
			add(new Label("praktijkNaam", enovationHuisarts.getPraktijknaam()));
			add(new Label("praktijkAdres", AdresUtil.getVolledigeAdresString(enovationHuisarts.getAdres())));
			String agbCode = enovationHuisarts.getHuisartsAgb();
			if (StringUtils.isBlank(agbCode))
			{
				agbCode = enovationHuisarts.getPraktijkAgb();
			}
			add(new Label("huisartsAgb", agbCode));
			add(new Label("weergaveNaam", enovationHuisarts.getWeergavenaam()));
			add(new Label("klantnummer", enovationHuisarts.getKlantnummer()));
			add(new Label("ediadres", enovationHuisarts.getOorspronkelijkEdiadres()));
			add(new Label("communicatieadres", enovationHuisarts.getEdiadres()));
		}
		else if (onbekendeHuisarts != null)
		{

			add(new Label("huisartsNaam", onbekendeHuisarts.getHuisartsNaam()));
			add(new Label("praktijkNaam", onbekendeHuisarts.getPraktijkNaam()));
			add(new Label("praktijkAdres", AdresUtil.getOnbekendeHuisartsAdres(onbekendeHuisarts)));
			add(new Label("huisartsAgb", Model.of("")));
			add(new Label("weergaveNaam", Model.of("")));
			add(new Label("klantnummer", Model.of("")));
			add(new Label("ediadres", Model.of("")));
			add(new Label("communicatieadres", Model.<String> of("")));
		}
		else
		{
			add(new Label("huisartsNaam", Model.of("")));
			add(new Label("praktijkNaam", Model.of("")));
			add(new Label("praktijkAdres", Model.of("")));
			add(new Label("huisartsAgb", Model.of("")));
			add(new Label("weergaveNaam", Model.of("")));
			add(new Label("klantnummer", Model.of("")));
			add(new Label("ediadres", Model.of("")));
			add(new Label("communicatieadres", Model.of("")));
		}
	}

	@Override
	protected void onDetach()
	{
		super.onDetach();
		ModelUtil.nullSafeDetach(getZoekModel());
	}

	public IModel<EnovationHuisarts> getZoekModel()
	{
		return zoekModel;
	}

	private void setZoekModel(IModel<EnovationHuisarts> zoekModel)
	{
		this.zoekModel = zoekModel;
	}

	private BootstrapDialog getDialog()
	{
		return dialog;
	}

	private void setDialog(BootstrapDialog dialog)
	{
		this.dialog = dialog;
	}

	private ColonHuisartsWijzigenPanel getHuisartsWijzigenPanel()
	{
		return huisartsWijzigenPanel;
	}

	private void setHuisartsWijzigenPanel(ColonHuisartsWijzigenPanel huisartsWijzigenPanel)
	{
		this.huisartsWijzigenPanel = huisartsWijzigenPanel;
	}
}