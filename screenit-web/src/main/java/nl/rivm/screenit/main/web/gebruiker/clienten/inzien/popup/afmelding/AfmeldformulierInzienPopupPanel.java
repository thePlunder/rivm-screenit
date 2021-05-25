
package nl.rivm.screenit.main.web.gebruiker.clienten.inzien.popup.afmelding;

/*-
 * ========================LICENSE_START=================================
 * screenit-web
 * %%
 * Copyright (C) 2012 - 2021 Facilitaire Samenwerking Bevolkingsonderzoek
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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import nl.rivm.screenit.comparator.BriefCreatieDatumComparator;
import nl.rivm.screenit.main.service.BriefService;
import nl.rivm.screenit.main.service.DossierService;
import nl.rivm.screenit.main.util.BriefOmschrijvingUtil;
import nl.rivm.screenit.main.web.ScreenitSession;
import nl.rivm.screenit.main.web.gebruiker.clienten.inzien.popup.DocumentVervangenPanel;
import nl.rivm.screenit.model.Afmelding;
import nl.rivm.screenit.model.enums.GebeurtenisBron;
import nl.rivm.screenit.model.ClientBrief;
import nl.rivm.screenit.model.DossierStatus;
import nl.rivm.screenit.model.UploadDocument;
import nl.rivm.screenit.model.cervix.CervixAfmelding;
import nl.rivm.screenit.model.cervix.enums.CervixAfmeldingReden;
import nl.rivm.screenit.model.colon.ColonAfmelding;
import nl.rivm.screenit.model.colon.enums.ColonAfmeldingReden;
import nl.rivm.screenit.model.enums.Actie;
import nl.rivm.screenit.model.enums.Bevolkingsonderzoek;
import nl.rivm.screenit.model.enums.BriefType;
import nl.rivm.screenit.model.enums.LogGebeurtenis;
import nl.rivm.screenit.model.enums.Recht;
import nl.rivm.screenit.model.mamma.MammaAfmelding;
import nl.rivm.screenit.model.mamma.enums.MammaAfmeldingReden;
import nl.rivm.screenit.service.BriefHerdrukkenService;
import nl.rivm.screenit.service.ClientService;
import nl.rivm.screenit.service.FileService;
import nl.rivm.screenit.service.LogService;
import nl.rivm.screenit.util.BriefUtil;
import nl.topicuszorg.hibernate.object.helper.HibernateHelper;
import nl.topicuszorg.hibernate.spring.dao.HibernateService;
import nl.topicuszorg.wicket.hibernate.util.ModelUtil;

import org.apache.commons.io.FilenameUtils;
import org.apache.shiro.util.CollectionUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.wicketstuff.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.EnumLabel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.hibernate.envers.query.AuditEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AfmeldformulierInzienPopupPanel<A extends Afmelding> extends GenericPanel<A>
{

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(AfmeldformulierInzienPopupPanel.class);

	@SpringBean
	private FileService fileService;

	@SpringBean
	private DossierService dossierService;

	@SpringBean
	private BriefService briefService;

	@SpringBean
	private BriefHerdrukkenService briefHerdrukkenService;

	@SpringBean
	private LogService logService;

	@SpringBean
	private HibernateService hibernateService;

	@SpringBean
	private ClientService clientService;

	private IModel<UploadDocument> document;

	private WebMarkupContainer uploadForm;

	private IModel<List<FileUpload>> files;

	public AfmeldformulierInzienPopupPanel(String id, IModel<A> model)
	{
		super(id, model);
		files = new ListModel<>();

		add(new Label("wijzeAfmelding", Model.of(getWijzeVanAfmeldingTekst(model.getObject()))));

		WebMarkupContainer verstuurdFormulierContainer = new WebMarkupContainer("formulierVerstuurdContainer");
		add(verstuurdFormulierContainer);

		verstuurdFormulierContainer.add(new ListView<String>("brievenLijst", creatieDatumCreaterAfmelding(model.getObject()))
		{

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<String> item)
			{
				String tekst = item.getModelObject();
				item.add(new Label("brief", Model.<String> of(tekst)));
			}

		});

		add(DateLabel.forDatePattern("afmeldDatum", new PropertyModel<Date>(model, "afmeldDatum"), "dd-MM-yyyy"));

		switch (getModelObject().getBevolkingsonderzoek())
		{
		case COLON:
			ColonAfmeldingReden colonAfmeldingReden = ((ColonAfmelding) getModelObject()).getReden();
			add(new EnumLabel<ColonAfmeldingReden>("reden", colonAfmeldingReden));
			break;
		case CERVIX:
			CervixAfmeldingReden cervixAfmeldingReden = ((CervixAfmelding) getModelObject()).getReden();
			add(new EnumLabel<CervixAfmeldingReden>("reden", cervixAfmeldingReden));
			break;
		case MAMMA:
			MammaAfmeldingReden mammaAfmeldingReden = ((MammaAfmelding) getModelObject()).getReden();
			add(new EnumLabel<MammaAfmeldingReden>("reden", mammaAfmeldingReden));
			break;
		}

		Date heroverwegersBriefVerstuurd = heeftHeroverwegersBrief();
		WebMarkupContainer heroverwegersBriefVerstuurdContainer = new WebMarkupContainer("heroverwegersbrief");
		heroverwegersBriefVerstuurdContainer.setVisible(heroverwegersBriefVerstuurd != null);
		add(heroverwegersBriefVerstuurdContainer);
		heroverwegersBriefVerstuurdContainer.add(DateLabel.forDatePattern("datumHeroverwegersbriefVerstuurd", new Model<>(heroverwegersBriefVerstuurd), "dd-MM-yyyy"));

		document = ModelUtil.sModel(getModelObject().getHandtekeningDocumentAfmelding());
		if (isDocumentBeschikbaar(document))
		{
			DownloadLink downloadLink = new DownloadLink("afmeldformulierHandImg", new LoadableDetachableModel<File>()
			{

				private static final long serialVersionUID = 1L;

				@Override
				protected File load()
				{
					return fileService.load(document.getObject());
				}

			}, "Afmeldingsformulier met handtekening." + FilenameUtils.getExtension(document.getObject().getNaam()));
			add(downloadLink);
		}
		else
		{
			EmptyPanel downloadLink = new EmptyPanel("afmeldformulierHandImg");
			downloadLink.setVisible(false);
			add(downloadLink);
		}

		addVervangenPanel(model);

		addButtons();
	}

	private Date heeftHeroverwegersBrief()
	{
		if (getModelObject().getBevolkingsonderzoek().equals(Bevolkingsonderzoek.CERVIX))
		{
			List<ClientBrief> brieven = getModelObject().getBrieven();
			for (ClientBrief brief : brieven)
			{
				if (brief.getBriefType().equals(BriefType.CERVIX_HEROVERWEGERS))
				{
					if (brief.getMergedBrieven() != null)
					{
						return brief.getMergedBrieven().getPrintDatum();
					}
				}
			}
		}
		return null;
	}

	private boolean isAfmeldingUitCISHistorie(A a)
	{
		if (HibernateHelper.deproxy(a) instanceof CervixAfmelding)
		{
			CervixAfmelding afmelding = (CervixAfmelding) a;
			return afmelding.getDossier().getCisHistorie() != null && afmelding.equals(afmelding.getDossier().getCisHistorie().getAfmelding());
		}
		return false;
	}

	private void addButtons()
	{
		add(new AjaxLink<Void>("nogmaalsVersturen")
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				ClientBrief brief = AfmeldformulierInzienPopupPanel.this.getModelObject().getAfmeldingBevestiging();
				briefHerdrukkenService.opnieuwAanmaken(brief, ScreenitSession.get().getLoggedInAccount());

				info(getString("info.afmeldingnogmaalsverstuurd"));
				close(target);
			}

		}.setVisible(DossierStatus.INACTIEF.equals(getModelObject().getDossier().getStatus()) && !isAfmeldingUitCISHistorie(getModelObject())));

		boolean magTegenhouden = ScreenitSession.get().checkPermission(Recht.GEBRUIKER_CLIENT_SR_BRIEVEN_TEGENHOUDEN, Actie.AANPASSEN);
		add(new AjaxLink<Void>("tegenhouden")
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				ClientBrief brief = getLaatsteBrief();
				brief.setTegenhouden(true);
				hibernateService.saveOrUpdate(brief);

				logService.logGebeurtenis(LogGebeurtenis.BRIEF_TEGENHOUDEN, ScreenitSession.get().getLoggedInAccount(), brief.getClient(),
					BriefUtil.getBriefTypeNaam(brief) + ", wordt tegengehouden.", brief.getBriefType().getOnderzoeken());
				info(getString("info.brieftegenhouden"));
				close(target);
			}
		}.setVisible(
			magTegenhouden && getLaatsteBrief() != null && !getLaatsteBrief().isTegenhouden() && getLaatsteBrief().getMergedBrieven() == null));
		add(new AjaxLink<Void>("doorvoeren")
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				ClientBrief brief = getLaatsteBrief();
				brief.setTegenhouden(false);
				hibernateService.saveOrUpdate(brief);
				logService.logGebeurtenis(LogGebeurtenis.BRIEF_DOORVOEREN, ScreenitSession.get().getLoggedInAccount(), brief.getClient(),
					brief.getBriefType() + ", was tegengehouden en wordt nu doorgevoerd.", brief.getBriefType().getOnderzoeken());
				info(getString("info.briefactiveren"));
				close(target);
			}
		}.setVisible(magTegenhouden && getLaatsteBrief() != null && getLaatsteBrief().isTegenhouden()));

		document = ModelUtil.sModel(getModelObject().getHandtekeningDocumentAfmelding());
		add(new AjaxLink<Void>("vervangen")
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				uploadForm.setVisible(true);
				target.add(uploadForm);
			}
		}.setVisible(ScreenitSession.get().checkPermission(Recht.VERVANGEN_DOCUMENTEN, Actie.AANPASSEN) && isDocumentBeschikbaar(document)));
	}

	private boolean isDocumentBeschikbaar(IModel<UploadDocument> document)
	{
		return document != null && document.getObject() != null;
	}

	private void addVervangenPanel(IModel<A> model)
	{
		uploadForm = new DocumentVervangenPanel("documentVervangen")
		{
			@Override
			protected void vervangDocument(UploadDocument uploadDocument, AjaxRequestTarget target)
			{
				if (clientService.vervangAfmeldingDocument(uploadDocument, model.getObject(), document.getObject(), getLaatsteBrief(), ScreenitSession.get().getLoggedInAccount()))
				{
					info(getString("info.vervangendocument"));
					close(target);
				}
				else
				{
					error(getString("error.onbekend"));
				}
			}
		};
		uploadForm.setVisible(false);
		uploadForm.setOutputMarkupId(true);
		uploadForm.setOutputMarkupPlaceholderTag(true);
		add(uploadForm);
	}

	private ClientBrief getLaatsteBrief()
	{
		List<? extends ClientBrief> brieven = briefService.getBrievenVanAfmelding(getModelObject(), false);
		Collections.sort(brieven, new BriefCreatieDatumComparator().reversed());
		if (!CollectionUtils.isEmpty(brieven))
		{
			return brieven.get(0);
		}
		return null;
	}

	private String getWijzeVanAfmeldingTekst(A afmelding)
	{
		String wijzeAfmelding = "";
		GebeurtenisBron bron = dossierService.bepaalGebeurtenisBron(afmelding,
			AuditEntity.and(AuditEntity.property("heraanmeldStatus").isNull(), AuditEntity.property("statusHeraanmeldDatum").isNull()));
		if (bron != null)
		{
			switch (bron)
			{
			case MEDEWERKER:
				if (Bevolkingsonderzoek.COLON.equals(afmelding.getBevolkingsonderzoek())
					&& (ColonAfmeldingReden.ONTERECHT.equals(((ColonAfmelding) HibernateHelper.deproxy(afmelding)).getReden())
						|| ((ColonAfmelding) HibernateHelper.deproxy(afmelding)).getReden() == null))
				{
					wijzeAfmelding = "correctieantwoordformulier";
				}
				else
				{
					wijzeAfmelding = "infolijn";
				}
				break;
			case AUTOMATISCH:
				if (Bevolkingsonderzoek.COLON.equals(afmelding.getBevolkingsonderzoek())
					&& ColonAfmeldingReden.ONTERECHT.equals(((ColonAfmelding) HibernateHelper.deproxy(afmelding)).getReden()))
				{
					wijzeAfmelding = "correctieantwoordformulier";
				}
				else
				{
					wijzeAfmelding = "antwoordformulier";
				}
				break;
			case CLIENT:
				wijzeAfmelding = "clientportaal";
				break;
			default:
				wijzeAfmelding = "";
			}
		}
		else if (Bevolkingsonderzoek.COLON.equals(afmelding.getBevolkingsonderzoek())
			&& ColonAfmeldingReden.PROEF_BEVOLKINGSONDERZOEK.equals(((ColonAfmelding) HibernateHelper.deproxy(afmelding)).getReden()))
		{
			wijzeAfmelding = "proefbevolkingsonderzoek";
		}
		else if (Bevolkingsonderzoek.MAMMA.equals(afmelding.getBevolkingsonderzoek()))
		{
			wijzeAfmelding = "infolijn"; 
		}

		if (wijzeAfmelding.equals(""))
		{
			return "";
		}

		return getString("label.wijzevanafmelding." + wijzeAfmelding);
	}

	private List<String> creatieDatumCreaterAfmelding(A afmelding)
	{
		List<? extends ClientBrief> brieven = briefService.getBrievenVanAfmelding(afmelding, false);
		return BriefOmschrijvingUtil.getBrievenOmschrijvingen(brieven, this::getString);
	}

	protected abstract void close(AjaxRequestTarget target);

	@Override
	protected void onDetach()
	{
		super.onDetach();
		ModelUtil.nullSafeDetach(document);
		ModelUtil.nullSafeDetach(files);
	}
}
