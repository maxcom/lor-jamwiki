package org.vqwiki.utils;

/*
Very Quick Wiki - WikiWikiWeb clone
Copyright (C) 2001-2002 Gareth Cronin

This program is free software; you can redistribute it and/or modify
it under the terms of the latest version of the GNU Lesser General
Public License as published by the Free Software Foundation;

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program (gpl.txt); if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/

public class ComparablePair implements Comparable {

	Comparable one;
	Object two;

	/**
	 *
	 */
	public ComparablePair(Comparable one, Object two) {
		this.one = one;
		this.two = two;
	}

	/**
	 *
	 */
	public int compareTo(Object o) {
		ComparablePair other = (ComparablePair) o;
		if (one.compareTo(other.getOne()) == 0) return -1;
		return -one.compareTo(other.getOne());
	}

	/**
	 *
	 */
	public Object getOne() {
		return this.one;
	}

	/**
	 *
	 */
	public Object getTwo() {
		return this.two;
	}

	/**
	 *
	 */
	public int hashCode() {
		return (two.hashCode() + one.hashCode()) % Integer.MAX_VALUE;
	}

	/**
	 *
	 */
	public boolean equals(Object o) {
		ComparablePair other = (ComparablePair) o;
		if (other.getOne().equals(this.one) && other.getTwo().equals(this.two)) {
			return true;
		}
		return false;
	}

	/**
	 *
	 */
	public String toString() {
		return this.one + ":" + this.two;
	}
}
