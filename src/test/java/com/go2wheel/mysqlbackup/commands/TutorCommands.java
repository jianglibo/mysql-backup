package com.go2wheel.mysqlbackup.commands;

import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.Size;

import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.standard.ValueProviderSupport;
import org.springframework.shell.table.AutoSizeConstraints;
import org.springframework.shell.table.BeanListTableModel;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.CellMatcher;
import org.springframework.shell.table.CellMatchers;
import org.springframework.shell.table.KeyValueHorizontalAligner;
import org.springframework.shell.table.KeyValueSizeConstraints;
import org.springframework.shell.table.KeyValueTextWrapper;
import org.springframework.shell.table.SimpleHorizontalAligner;
import org.springframework.shell.table.Table;
import org.springframework.shell.table.TableBuilder;
import org.springframework.shell.table.TableModel;
import org.springframework.shell.table.TableModelBuilder;
import org.springframework.stereotype.Component;

@ShellComponent()
public class TutorCommands {
	@ShellMethod(value = "A command whose name looks the same as another one.", key = "help me out")
	public void helpMeOut() {
		System.out.println("You can go");
	}

	@ShellMethod("Change Password. Shows support for bean validation.")
	public String changePassword(@Size(min = 8) String password) {
		return "Password changed";
	}
	
	@ShellMethod(value = "Shows non trivial character encoding.")
	public String helloWorld() {
		return "こんにちは世界";
	}

	@ShellMethod("Shows support for boolean parameters, with arity=0.")
	public void shutdown(@ShellOption(arity = 0) boolean force) {
		System.out.println("You passed " + force);
	}

	@ShellMethod("Test completion of special values.")
	public void quote(@ShellOption(valueProvider = FunnyValuesProvider.class) String text) {
		System.out.println("You said " + text);
	}

	@ShellMethod("Add numbers.")
	public int add(int a, int b, int c) {
		return a + b + c;
	}

	@ShellMethod("Fails with an exception. Shows enum conversion.")
	public void fail(ElementType elementType) {
		throw new IllegalArgumentException("You said " + elementType);
	}

	@ShellMethod("Add array numbers.")
	public double addDoubles(@ShellOption(arity = 3) double[] numbers) {
		return Arrays.stream(numbers).sum();
	}
	
	
	@ShellMethod("make a table.")
	public String makeATable() {
		TableModelBuilder<String> tmb = new TableModelBuilder<>();
		tmb.addRow();
		tmb.addValue("hello");
		tmb.addValue("world");
		
		tmb.addRow();
		tmb.addValue("your");
		tmb.addValue("dreams");
		TableBuilder tb = new TableBuilder(tmb.build());
		tb.addFullBorder(BorderStyle.fancy_double);
		tb.on(CellMatchers.table()).addAligner(SimpleHorizontalAligner.center);
		tb.on(CellMatchers.table()).addSizer(new AutoSizeConstraints());
		
		Table tbl = tb.build();
		
		return tbl.render(80);
	}
	
	@ShellMethod("make a map table.")
	public Table makeABeanTable() {
		LinkedHashMap<String, Object> headerm = new LinkedHashMap<>();
		headerm.put("acode", "action Code");
		headerm.put("description", "action Content");
		headerm.put("third", "third");
		List<Tm3> models = new ArrayList<>();

		models.add(new Tm3(1, "username", "third"));
		models.add(new Tm3(2, "password", "third"));
		TableModel tm = new BeanListTableModel<Tm3>(models, headerm);
		
		TableBuilder tb = new TableBuilder(tm);
		tb.addFullBorder(BorderStyle.fancy_light);
		tb.on(CellMatchers.table()).addAligner(SimpleHorizontalAligner.center);
		tb.on(CellMatchers.table()).addSizer(new AutoSizeConstraints());
		Table tbl = tb.build();
		
		return tbl;
	}
	
	@ShellMethod("make a map table.")
	public Table makeAMapTable() {
		LinkedHashMap<String, Object> headerm = new LinkedHashMap<>();
		headerm.put("acode", "action Code");
		headerm.put("map", "a map");
		List<TmMap> models = new ArrayList<>();

		models.add(new TmMap(1, "username=hello owrld"));
		models.add(new TmMap(2, "password a helodl= third"));
		TableModel tm = new BeanListTableModel<TmMap>(models, headerm);
		
		TableBuilder tb = new TableBuilder(tm);
		tb.addFullBorder(BorderStyle.fancy_light);
//		tb.on(CellMatchers.table()).addAligner(SimpleHorizontalAligner.center);
		tb.on(new CellMatcher() {
			@Override
			public boolean matches(int row, int column, TableModel model) {
				return column != 1 || row == 0;
			}
		}).addSizer(new AutoSizeConstraints());
		tb.on(new CellMatcher() {
			@Override
			public boolean matches(int row, int column, TableModel model) {
				return column == 1 && row > 0;
			}
		}).addWrapper(new KeyValueTextWrapper("=")).addAligner(new KeyValueHorizontalAligner("=")).addSizer(new KeyValueSizeConstraints("="));
		Table tbl = tb.build();
		
		return tbl;
	}
	
}

/**
 * A {@link org.springframework.shell.standard.ValueProvider} that emits values with special characters
 * (quotes, escapes, <em>etc.</em>)
 *
 * @author Eric Bottard
 */
@Component
class FunnyValuesProvider extends ValueProviderSupport {

	private final static String[] VALUES = new String[] {
		"hello world",
		"I'm quoting \"The Daily Mail\"",
		"10 \\ 3 = 3"
	};

	@Override
	public List<CompletionProposal> complete(MethodParameter parameter, CompletionContext completionContext, String[] hints) {
		return Arrays.stream(VALUES).map(CompletionProposal::new).collect(Collectors.toList());
	}
}
