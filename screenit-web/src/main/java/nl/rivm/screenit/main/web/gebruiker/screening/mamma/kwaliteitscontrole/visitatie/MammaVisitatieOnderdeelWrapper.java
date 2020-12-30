package nl.rivm.screenit.main.web.gebruiker.screening.mamma.kwaliteitscontrole.visitatie;

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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import nl.rivm.screenit.main.util.EnumStringUtil;
import nl.rivm.screenit.main.web.gebruiker.base.GebruikerMenuItem;
import nl.rivm.screenit.model.mamma.enums.MammaVisitatieOnderdeel;

public enum MammaVisitatieOnderdeelWrapper
{
	INSTELTECHNIEK(MammaVisitatieOnderzoekenOnderdeelInsteltechniekWerklijstPage.class),

	INTERVALCARCINOMEN(MammaVisitatieOnderzoekenOnderdeelIntervalcarcinomenWerklijstPage.class),

	T2_PLUS_SCREEN_DETECTED(MammaVisitatieOnderzoekenOnderdeelT2PlusScreenDetectedWerklijstPage.class),

	VERWIJZINGEN(MammaVisitatieOnderzoekenOnderdeelVerwijzingenWerklijstPage.class),

	PROTHESES(MammaVisitatieOnderzoekenOnderdeelProthesesWerklijstPage.class);

	private final Class<? extends MammaVisitatieOnderzoekenWerklijstPage> pageClass;

	MammaVisitatieOnderdeelWrapper(Class<? extends MammaVisitatieOnderzoekenWerklijstPage> pageClass)
	{
		this.pageClass = pageClass;
	}

	public static List<GebruikerMenuItem> getContextMenuItems()
	{
		return Arrays.asList(values()).stream().map(ow -> new GebruikerMenuItem(EnumStringUtil.getPropertyString(MammaVisitatieOnderdeel.valueOf(ow.name())), true,
			ow.pageClass)).collect(Collectors.toList());
	}
}
