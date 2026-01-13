package com.example.demo.service;

import org.springframework.stereotype.Service;
import java.time.*;
import java.util.*;

@Service
public class CalendarioService {

	private static final Set<MonthDay> FERIADOS_FIJOS = Set.of(MonthDay.of(1, 1), MonthDay.of(2, 5), MonthDay.of(3, 21),
			MonthDay.of(5, 1), MonthDay.of(9, 16), MonthDay.of(11, 20), MonthDay.of(12, 25));

	private LocalDate primerLunes(int year, int mes) {
		LocalDate date = LocalDate.of(year, mes, 1);
		while (date.getDayOfWeek() != DayOfWeek.MONDAY) {
			date = date.plusDays(1);
		}
		return date;
	}

	private LocalDate tercerLunes(int year, int mes) {
		LocalDate date = LocalDate.of(year, mes, 1);
		int count = 0;

		while (true) {
			if (date.getDayOfWeek() == DayOfWeek.MONDAY)
				count++;
			if (count == 3)
				return date;
			date = date.plusDays(1);
		}
	}

	public boolean esDiaCerrado(LocalDate fecha) {
		int year = fecha.getYear();

		if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY)
			return true;

		if (FERIADOS_FIJOS.contains(MonthDay.from(fecha)))
			return true;

		if (fecha.equals(primerLunes(year, 2)))
			return true;
		if (fecha.equals(tercerLunes(year, 3)))
			return true;
		if (fecha.equals(tercerLunes(year, 11)))
			return true;

		return false;
	}
}
